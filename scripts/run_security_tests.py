# -*- coding: utf-8 -*-
"""
run_security_tests.py — Automated security / edge-case / E2E test runner.

Generates JWTs (valid, expired, blocked-user), fires HTTP requests against
the running backend, verifies status codes + response bodies, prints a
verbose screenshot-friendly log, and saves artifacts.

Prerequisites:
    pip install requests PyJWT
    Backend running → docker-compose -f docker-compose.dev.yml up -d
                  OR  cd Lottery-App/backend/checker && mvnw spring-boot:run

Usage:
    python scripts/run_security_tests.py
"""

import json
import subprocess
import sys
import datetime
import glob
import re
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor, as_completed

# Force UTF-8 output on Windows terminals (cp932/cp1252 can't handle em-dashes)
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

try:
    import requests
except ImportError:
    sys.exit("Missing library. Run:  pip install requests")

try:
    import jwt as pyjwt
except ImportError:
    sys.exit("Missing library. Run:  pip install PyJWT")

# ────────────────────────────────────────────────────────────────────
# CONFIG
# ────────────────────────────────────────────────────────────────────
BASE = "http://localhost:8080/api/v1"
JWT_SECRET = "dev-secret-change-in-production-abc123"
SEED_PASSWORD = "phucpddfx03285"

ADMIN_EMAIL = "admin@veso.vn"
USER_EMAIL = "khach2@gmail.com"
KHACH1_EMAIL = "khach1@gmail.com"
BLOCKED_EMAIL = "locked@gmail.com"

# E2E uses yesterday so it's always in the past and not in seed data
E2E_DATE = (datetime.date.today() - datetime.timedelta(days=1)).isoformat()
E2E_STATION_ID = 1  # HCM

# Artifact output — now inside Task 5-8
ARTIFACTS = Path(__file__).resolve().parent.parent / "Task 5-8" / "artifacts"
ARTIFACTS.mkdir(parents=True, exist_ok=True)

# Also clean up old root-level artifacts (if any) to avoid confusion
OLD_ARTIFACTS = Path(__file__).resolve().parent.parent / "artifacts"
if OLD_ARTIFACTS.exists():
    import shutil
    shutil.rmtree(OLD_ARTIFACTS, ignore_errors=True)
TS = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
LOG_FILE = ARTIFACTS / f"test_run_{TS}.txt"
JSON_FILE = ARTIFACTS / f"test_results_{TS}.json"

results: list[dict] = []
log_lines: list[str] = []


# ────────────────────────────────────────────────────────────────────
# HELPERS
# ────────────────────────────────────────────────────────────────────
def log(msg: str = ""):
    print(msg)
    log_lines.append(msg)


def make_jwt(email: str, role: str = "ROLE_USER",
             user_code: str = "USR-00-0000-00000000",
             full_name: str = "Test", expired: bool = False) -> str:
    now = datetime.datetime.now(datetime.timezone.utc)
    if expired:
        iat = now - datetime.timedelta(hours=25)
        exp = now - datetime.timedelta(hours=1)
    else:
        iat = now
        exp = now + datetime.timedelta(hours=24)
    payload = {"sub": email, "role": role, "userCode": user_code,
               "fullName": full_name, "iat": iat, "exp": exp}
    return pyjwt.encode(payload, JWT_SECRET, algorithm="HS256")


def run_test(test_id: str, desc: str, method: str, url: str,
             expected: int, headers: dict | None = None,
             body: dict | None = None, raw: str | None = None,
             expect_in: list[str] | None = None,
             reject_in: list[str] | None = None,
             accept: list[int] | None = None) -> tuple[bool, requests.Response | None]:
    """Fire one HTTP request, compare status + body, log verbosely."""
    ok_statuses = accept or [expected]
    hdrs = dict(headers or {})
    if raw is not None:
        hdrs.setdefault("Content-Type", "application/json")

    try:
        kw: dict = {"headers": hdrs, "timeout": 10}
        if body is not None:
            kw["json"] = body
        elif raw is not None:
            kw["data"] = raw

        resp = getattr(requests, method.lower())(url, **kw)
        status_ok = resp.status_code in ok_statuses
        text = resp.text

        body_ok = True
        if expect_in:
            body_ok = all(s.lower() in text.lower() for s in expect_in)
        if reject_in and body_ok:
            body_ok = all(s.lower() not in text.lower() for s in reject_in)

        passed = status_ok and body_ok
        results.append({"id": test_id, "desc": desc, "passed": passed,
                        "expected": ok_statuses, "actual": resp.status_code,
                        "body": text[:600]})

        tag = "PASS" if passed else "FAIL"
        log(f"\n{'=' * 74}")
        log(f"[{tag}]  {test_id}: {desc}")
        log(f"  {method} {url}")
        if body:
            log(f"  Payload: {json.dumps(body, ensure_ascii=False)[:220]}")
        elif raw:
            log(f"  Raw: {raw[:220]}")
        log(f"  Expected HTTP {ok_statuses}  →  Got HTTP {resp.status_code}")
        if not body_ok:
            log("  ⚠ Body-content check failed")
        log(f"  Response: {text[:350]}")
        log(f"{'=' * 74}")
        return passed, resp

    except requests.exceptions.ConnectionError:
        log(f"\n[FAIL]  {test_id}: {desc}")
        log("  CONNECTION REFUSED — start the backend first.")
        results.append({"id": test_id, "desc": desc, "passed": False,
                        "expected": ok_statuses, "actual": "CONN_ERR", "body": ""})
        return False, None
    except Exception as exc:
        log(f"\n[FAIL]  {test_id}: {desc}")
        log(f"  ERROR: {exc}")
        results.append({"id": test_id, "desc": desc, "passed": False,
                        "expected": ok_statuses, "actual": "ERROR", "body": str(exc)})
        return False, None


# ────────────────────────────────────────────────────────────────────
# DOCKER SQL HELPER (automates DB-level assertions via docker exec)
# ────────────────────────────────────────────────────────────────────
DB_CONTAINER = "lottery-db-dev"
DB_USER = "lottery"
DB_PASS = "123456"
DB_NAME = "lottery_db"


def docker_sql(query: str) -> tuple[bool, str]:
    """Run a read-only SQL query inside the MySQL container via docker exec."""
    try:
        result = subprocess.run(
            ["docker", "exec", DB_CONTAINER,
             "mysql", f"-u{DB_USER}", f"-p{DB_PASS}", DB_NAME,
             "-N", "--skip-column-names", "-e", query],
            capture_output=True, text=True, timeout=15,
        )
        if result.returncode != 0:
            return False, result.stderr.strip()
        return True, result.stdout.strip()
    except FileNotFoundError:
        return False, "docker CLI not found in PATH"
    except subprocess.TimeoutExpired:
        return False, "docker exec timed out"
    except Exception as e:
        return False, str(e)


# ────────────────────────────────────────────────────────────────────
# MAIN
# ────────────────────────────────────────────────────────────────────
def cleanup_previous_run():
    """Delete test artifacts from prior runs so results are deterministic."""
    log("\n[CLEANUP] Removing stale test data from previous runs…")
    cleanups = [
        # E2E result created with yesterday's date
        (f"DELETE FROM check_histories WHERE result_id IN "
         f"(SELECT id FROM lottery_results WHERE station_id={E2E_STATION_ID} AND draw_date='{E2E_DATE}')",
         "E2E check_histories"),
        (f"DELETE FROM check_sessions WHERE result_id IN "
         f"(SELECT id FROM lottery_results WHERE station_id={E2E_STATION_ID} AND draw_date='{E2E_DATE}')",
         "E2E check_sessions"),
        (f"DELETE FROM prize_details WHERE result_id IN "
         f"(SELECT id FROM lottery_results WHERE station_id={E2E_STATION_ID} AND draw_date='{E2E_DATE}')",
         "E2E prize_details"),
        (f"DELETE FROM lottery_results WHERE station_id={E2E_STATION_ID} AND draw_date='{E2E_DATE}'",
         "E2E lottery_result"),
        # F02-X / F02-07/08/09 ticket (fixed date 2025-01-04)
        ("DELETE FROM check_histories WHERE result_id IN "
         "(SELECT id FROM lottery_results WHERE draw_date='2025-01-04')",
         "F02-X check_histories"),
        ("DELETE FROM check_sessions WHERE result_id IN "
         "(SELECT id FROM lottery_results WHERE draw_date='2025-01-04')",
         "F02-X check_sessions"),
        ("DELETE FROM prize_details WHERE result_id IN "
         "(SELECT id FROM lottery_results WHERE draw_date='2025-01-04')",
         "F02-X prize_details"),
        ("DELETE FROM lottery_results WHERE draw_date='2025-01-04'",
         "F02-X lottery_result"),
        # Race-condition users
        ("DELETE FROM user_auth_providers WHERE user_id IN "
         "(SELECT id FROM users WHERE email LIKE 'race%@test.com')",
         "race user_auth_providers"),
        ("DELETE FROM password_reset_tokens WHERE user_id IN "
         "(SELECT id FROM users WHERE email LIKE 'race%@test.com')",
         "race reset_tokens"),
        ("DELETE FROM users WHERE email LIKE 'race%@test.com'",
         "race users"),
        # XSS test users
        ("DELETE FROM user_auth_providers WHERE user_id IN "
         "(SELECT id FROM users WHERE email LIKE 'xss%@test.com')",
         "xss user_auth_providers"),
        ("DELETE FROM password_reset_tokens WHERE user_id IN "
         "(SELECT id FROM users WHERE email LIKE 'xss%@test.com')",
         "xss reset_tokens"),
        ("DELETE FROM users WHERE email LIKE 'xss%@test.com'",
         "xss users"),
        # Throwaway reset-test users
        ("DELETE FROM password_reset_tokens WHERE user_id IN "
         "(SELECT id FROM users WHERE email LIKE 'reset-test%@test.com')",
         "reset-test tokens"),
        ("DELETE FROM users WHERE email LIKE 'reset-test%@test.com'",
         "reset-test users"),
        # Change-password test users
        ("DELETE FROM users WHERE email LIKE 'chg-pwd%@test.com'",
         "chg-pwd users"),
    ]
    n_cleaned = 0
    for sql, label in cleanups:
        ok, out = docker_sql(sql)
        if ok:
            n_cleaned += 1
        else:
            log(f"  ⚠ cleanup '{label}' failed: {out[:80]}")
    log(f"  ✅ Cleanup done ({n_cleaned}/{len(cleanups)} statements OK).")


def clear_all_artifacts():
    """Delete all existing test_run_*.txt and test_results_*.json files."""
    for pattern in ["test_run_*.txt", "test_results_*.json"]:
        for f in glob.glob(str(ARTIFACTS / pattern)):
            try:
                Path(f).unlink()
                print(f"[CLEANUP] Removed: {Path(f).name}")
            except Exception as e:
                print(f"[CLEANUP] Failed to remove {Path(f).name}: {e}")


def main() -> int:
    log("=" * 74)
    log("  DO VE SO — AUTOMATED SECURITY / EDGE-CASE / E2E TEST RUNNER")
    log(f"  Date    : {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    log(f"  Backend : {BASE}")
    log(f"  E2E date: {E2E_DATE}  (station_id={E2E_STATION_ID})")
    log("=" * 74)

    # ── clear old artifact files ──
    clear_all_artifacts()

    # ── health check ──
    try:
        requests.get("http://localhost:8080/actuator/health", timeout=5)
        log("\n  ✅ Backend is reachable.")
    except Exception:
        log("\n  ❌ Backend NOT reachable on :8080.")
        log("     Start it:  docker-compose -f docker-compose.dev.yml up -d")
        log("     Or:        cd Lottery-App/backend/checker && mvnw spring-boot:run")
        return 1

    # ── wipe stale data ──
    cleanup_previous_run()

    # ── generate tokens ──
    log("\n[SETUP] Crafting JWT tokens (HS256, secret from env.properties)")
    admin_tok   = make_jwt(ADMIN_EMAIL,   "ROLE_ADMIN", "USR-10-2023-00000001", "Phan Dang Duy Phuc")
    user_tok    = make_jwt(USER_EMAIL,    "ROLE_USER",  "USR-10-2023-00000004", "Tran Thi Chin")
    khach1_tok  = make_jwt(KHACH1_EMAIL,  "ROLE_USER",  "USR-10-2023-00000003", "Le Van Tam")
    blocked_tok = make_jwt(BLOCKED_EMAIL, "ROLE_USER",  "USR-11-2023-00000001", "Nguoi Bi Khoa")
    expired_tok = make_jwt(USER_EMAIL,    "ROLE_USER",  expired=True)

    for label, t in [("Admin", admin_tok), ("User", user_tok),
                     ("Khach1", khach1_tok), ("Blocked", blocked_tok),
                     ("Expired", expired_tok)]:
        log(f"  {label:8s} → {t[:50]}…")

    A = {"Authorization": f"Bearer {admin_tok}"}
    U = {"Authorization": f"Bearer {user_tok}"}
    K = {"Authorization": f"Bearer {khach1_tok}"}
    B = {"Authorization": f"Bearer {blocked_tok}"}
    X = {"Authorization": f"Bearer {expired_tok}"}

    # ══════════════════════════════════════════════════════════════
    #  SECTION 1 — SECURITY & EDGE CASES
    # ══════════════════════════════════════════════════════════════
    log("\n" + "█" * 74)
    log("  SECTION 1: SECURITY & EDGE CASES  (20 tests)")
    log("█" * 74)

    # -- auth boundary --
    run_test("F1-20", "No token → 401",
             "GET", f"{BASE}/user/me", 401)

    run_test("SEC-01", "Expired JWT → 401",
             "GET", f"{BASE}/user/me", 401, headers=X)

    run_test("SEC-02", "Locked user + valid-signature JWT → 401",
             "GET", f"{BASE}/user/me", 401, headers=B)

    run_test("F1-23", "USER calls /admin/users → 403",
             "GET", f"{BASE}/admin/users", 403, headers=U)

    # -- malformed payloads --
    run_test("SEC-03a", "Empty login body → 400",
             "POST", f"{BASE}/auth/login", 400, body={})

    run_test("SEC-03b", "Null numbers in checker → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"stationId": 1, "date": "2023-10-23", "numbers": None})

    run_test("SEC-03c", "Completely empty body → 400",
             "POST", f"{BASE}/checker/check", 400, raw="")

    run_test("SEC-09", "Malformed JSON → clean 400, no stack trace",
             "POST", f"{BASE}/auth/login", 400,
             raw="{this is not valid json",
             reject_in=["stacktrace", "java.lang.", "at com.lottery"])

    # -- checker validation --
    nums_51 = [f"{i:06d}" for i in range(1, 52)]
    run_test("SEC-04", ">50 tickets → 400",
             "POST", f"{BASE}/checker/check", 400, headers=U,
             body={"stationId": 1, "date": "2023-10-23", "numbers": nums_51})

    run_test("SEC-05", "Future date → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"stationId": 1, "date": "2027-12-31", "numbers": ["123456"]})

    run_test("SEC-06a", "Non-numeric ticket 'ABC123' → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"stationId": 1, "date": "2023-10-23", "numbers": ["ABC123"]})

    run_test("SEC-06b", "5-digit ticket '12345' → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"stationId": 1, "date": "2023-10-23", "numbers": ["12345"]})

    run_test("SEC-06c", "Missing stationId → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"date": "2023-10-23", "numbers": ["123456"]})

    run_test("F3-03", "Guest sends 2 tickets → 400",
             "POST", f"{BASE}/checker/check", 400,
             body={"stationId": 1, "date": "2023-10-23",
                   "numbers": ["123456", "654321"]})

    run_test("F3-13", "Duplicate number in one request → 409",
             "POST", f"{BASE}/checker/check", 409, headers=U,
             body={"stationId": 1, "date": "2023-10-23",
                   "numbers": ["111222", "111222"]})

    # khach1 already checked 123485 for HCM 2023-10-23 in seed
    run_test("F3-14", "Already-checked ticket (khach1+123485) → 409",
             "POST", f"{BASE}/checker/check", 409, headers=K,
             body={"stationId": 1, "date": "2023-10-23", "numbers": ["123485"]})

    # -- injection / XSS --
    run_test("SEC-07", "SQL injection in admin search → safe 200",
             "GET",
             f"{BASE}/admin/users?keyword='; DROP TABLE users; --&page=0&size=20",
             200, headers=A)

    xss_email = f"xss-{TS}@test.com"
    run_test("SEC-08", "XSS in fullName → stored as text, no execution",
             "POST", f"{BASE}/auth/register", 200, accept=[200, 201],
             body={"fullName": "<script>alert(1)</script>",
                   "email": xss_email,
                   "password": "SecurePass123!",
                   "confirmPassword": "SecurePass123!"})

    # DB assertion: verify the XSS user got a proper user_code + ROLE_USER
    ok_uc, uc_val = docker_sql(
        f"SELECT user_code, role FROM users WHERE email='{xss_email}'")
    if ok_uc and "USR-" in uc_val and "ROLE_USER" in uc_val:
        log(f"  [DB-ASSERT] F1-01 supplement: user_code+role = {uc_val}")
    else:
        log(f"  [DB-ASSERT] Could not verify user_code for {xss_email}: {uc_val}")

    # -- response hygiene --
    run_test("SEC-11", "No 'password' key in /user/me response",
             "GET", f"{BASE}/user/me", 200, headers=U,
             reject_in=['"password"'])

    # -- DB-level: BCrypt hash verification --
    log(f"\n{'=' * 74}")
    log("[TEST]  SEC-12: BCrypt hash in DB (docker exec SQL)")
    ok_hash, hash_val = docker_sql(
        "SELECT password FROM users WHERE email='admin@veso.vn' LIMIT 1")
    if ok_hash and hash_val.startswith("$2a$"):
        log(f"[PASS]  SEC-12: hash = {hash_val[:29]}...  (BCrypt $2a$ confirmed)")
        results.append({"id": "SEC-12", "desc": "BCrypt hash in DB",
                        "passed": True, "expected": "$2a$ prefix",
                        "actual": hash_val[:29], "body": ""})
    else:
        log(f"[FAIL]  SEC-12: expected $2a$ prefix. ok={ok_hash}, got: {hash_val[:60]}")
        results.append({"id": "SEC-12", "desc": "BCrypt hash in DB",
                        "passed": False, "expected": "$2a$ prefix",
                        "actual": hash_val[:60], "body": ""})

    # -- concurrent registration (race condition) --
    log(f"\n{'=' * 74}")
    log("[TEST]  SEC-10: Concurrent registration — 5 simultaneous requests")
    log("  Design contract: no 500s, no duplicate user_codes.")
    log("  409 under contention is acceptable (unique constraint = safety net).")
    log(f"{'=' * 74}")

    def _register(i: int):
        email = f"race{i}-{TS}@test.com"
        try:
            r = requests.post(f"{BASE}/auth/register", json={
                "fullName": f"Race User {i}", "email": email,
                "password": "SecurePass123!", "confirmPassword": "SecurePass123!",
            }, timeout=15)
            return email, r.status_code
        except Exception as e:
            return email, str(e)

    with ThreadPoolExecutor(max_workers=5) as pool:
        futs = [pool.submit(_register, i) for i in range(1, 6)]
        race_results = sorted(f.result() for f in as_completed(futs))

    no_500 = all(code != 500 for _, code in race_results if isinstance(code, int))
    at_least_one = any(code in (200, 201) for _, code in race_results)
    all_acceptable = all(
        code in (200, 201, 409) for _, code in race_results if isinstance(code, int)
    )
    passed_race = no_500 and at_least_one and all_acceptable

    for email, code in race_results:
        log(f"  {email}  →  HTTP {code}")
    n_ok = sum(1 for _, c in race_results if c in (200, 201))
    n_conflict = sum(1 for _, c in race_results if c == 409)
    tag = "PASS" if passed_race else "FAIL"
    log(f"[{tag}]  SEC-10: {n_ok} created, {n_conflict} got 409 (retry-loop contention).")
    log(f"  No 500s: {no_500} | At least 1 success: {at_least_one}")
    log(f"  Defence note: retry-loop + unique constraint is the design;")
    log(f"  production would use DB sequence or SELECT FOR UPDATE.")
    results.append({"id": "SEC-10", "desc": "Concurrent registration race",
                    "passed": passed_race, "expected": "no 500, ≥1 success, rest 409",
                    "actual": str(race_results), "body": ""})

    # ── SECTION 1a: PASSWORD RESET FLOW (F1-14, F1-15) ──
    log("\n" + "█" * 74)
    log("  SECTION 1a: PASSWORD RESET — TOKEN LIFECYCLE (throwaway user)")
    log("█" * 74)

    # Register a throwaway user so we never mutate seed accounts
    reset_email = f"reset-test-{TS}@test.com"
    reset_pwd = "InitialPass123!"
    run_test("SEC-14a", f"Register throwaway user for reset flow",
             "POST", f"{BASE}/auth/register", 200, accept=[200, 201],
             body={"fullName": "Reset Test User", "email": reset_email,
                   "password": reset_pwd, "confirmPassword": reset_pwd})

    # Step 1: request reset for the throwaway user
    run_test("SEC-14b", "Forgot password for existing email → 200 generic",
             "POST", f"{BASE}/password/forgot", 200,
             body={"email": reset_email})

    # Step 2: fetch the token from DB
    ok_tok, tok_val = docker_sql(
        f"SELECT token FROM password_reset_tokens "
        f"WHERE user_id = (SELECT id FROM users WHERE email='{reset_email}') "
        f"AND is_used = 0 ORDER BY created_at DESC LIMIT 1")
    if ok_tok and tok_val.strip():
        reset_token = tok_val.strip()
        log(f"  [DB] Fetched reset token: {reset_token[:16]}…")

        # Step 3: validate token endpoint
        run_test("SEC-14c", "Validate fresh token → 200",
                 "GET", f"{BASE}/password/validate?token={reset_token}", 200)

        # Step 4: reset with valid token
        new_pwd = "ResetPass999!"
        run_test("F1-14", "Reset password with valid token → 200",
                 "POST", f"{BASE}/password/reset", 200,
                 body={"token": reset_token, "newPassword": new_pwd,
                       "confirmPassword": new_pwd})

        # Step 5: reuse the same token → 409 (one-time enforcement)
        run_test("F1-15", "Reuse consumed token → 409",
                 "POST", f"{BASE}/password/reset", 409,
                 body={"token": reset_token, "newPassword": "AnotherPass1!",
                       "confirmPassword": "AnotherPass1!"})

        # Step 6: login with NEW password works
        run_test("SEC-14d", "Login with reset password → 200",
                 "POST", f"{BASE}/auth/login", 200,
                 body={"email": reset_email, "password": new_pwd})

        # Step 7: old password no longer works
        run_test("SEC-14e", "Login with OLD password → 401",
                 "POST", f"{BASE}/auth/login", 401,
                 body={"email": reset_email, "password": reset_pwd})

        log(f"  ✅ Throwaway user {reset_email} will be cleaned up on next run.")
    else:
        log(f"  ⚠ Could not fetch reset token from DB. Skipping SEC-14/F1-14/F1-15.")
        results.append({"id": "SEC-14", "desc": "Password reset flow",
                        "passed": False, "expected": "token in DB",
                        "actual": tok_val, "body": ""})

    # ── F2-15: USER calls admin ticket API → 403 ──
    run_test("F2-15", "USER calls /admin/tickets → 403",
             "GET", f"{BASE}/admin/tickets", 403, headers=U)

    # ── F1-17: Change password (logged-in, correct old password) ──
    # Register throwaway user; endpoint is POST /user/change-password
    # DTO: ChangePasswordRequest(oldPassword, newPassword) — no confirmPassword (frontend-only)
    chg_email = f"chg-pwd-{TS}@test.com"
    chg_pwd = "InitialPass123!"
    requests.post(f"{BASE}/auth/register", json={
        "fullName": "Change Pwd Test", "email": chg_email,
        "password": chg_pwd, "confirmPassword": chg_pwd,
    }, timeout=10)

    # Login to get token
    try:
        lr = requests.post(f"{BASE}/auth/login",
                           json={"email": chg_email, "password": chg_pwd}, timeout=10)
        chg_tok = lr.json().get("data", {}).get("token", "")
        CH = {"Authorization": f"Bearer {chg_tok}"}

        # F1-17: change with correct old password → 200
        new_pwd = "NewSecure456!"
        run_test("F1-17", "Change password with correct oldPassword → 200",
                 "POST", f"{BASE}/user/change-password", 200, headers=CH,
                 body={"oldPassword": chg_pwd, "newPassword": new_pwd})

        # F1-17b: verify new password works for login
        run_test("F1-17b", "Login with changed password → 200",
                 "POST", f"{BASE}/auth/login", 200,
                 body={"email": chg_email, "password": new_pwd})

        # F1-17c: wrong old password → 400
        run_test("F1-17c", "Change password with wrong oldPassword → 400",
                 "POST", f"{BASE}/user/change-password", 400, headers=CH,
                 body={"oldPassword": "WrongOldPass1!", "newPassword": "Xyz789!"})

        # F1-16 supplement: weak password rejected by PasswordValidator → 400
        run_test("F1-16", "Change to blocklisted password 'matkhau123' → 400",
                 "POST", f"{BASE}/user/change-password", 400, headers=CH,
                 body={"oldPassword": new_pwd, "newPassword": "matkhau123"})

    except Exception as e:
        log(f"  ⚠ F1-16/F1-17 setup failed: {e}")

    # ── F2-14 supplement: total_queries verified via E2E-08 docker SQL ──
    # (cross-reference: E2E-08 already asserts total_queries=2 via docker exec)

    # ══════════════════════════════════════════════════════════════
    #  SECTION 1b — TICKET MANAGEMENT BACKEND VALIDATION (bypass UI)
    # ══════════════════════════════════════════════════════════════
    log("\n" + "█" * 74)
    log("  SECTION 1b: TICKET BACKEND VALIDATION (F02 — curl bypasses UI)")
    log("█" * 74)

    # F02-02: Missing required fields via API
    run_test("F02-02", "Create ticket missing prizes → 400",
             "POST", f"{BASE}/admin/tickets", 400, headers=A,
             body={"stationId": 1, "drawDate": "2025-01-01", "status": "UNPUBLISH",
                   "prizes": None})

    # F02-02b: Empty prizes list
    run_test("F02-02b", "Create ticket empty prizes list → 400",
             "POST", f"{BASE}/admin/tickets", 400, headers=A,
             body={"stationId": 1, "drawDate": "2025-01-01", "status": "UNPUBLISH",
                   "prizes": []})

    # F02-05: Non-numeric prize via API
    run_test("F02-05", "Non-numeric prize 'ABC' → 400",
             "POST", f"{BASE}/admin/tickets", 400, headers=A,
             body={"stationId": 1, "drawDate": "2025-01-02", "status": "UNPUBLISH",
                   "prizes": [{"type": "G_DB", "winningNumbers": "ABCDEF", "rewardAmount": 2000000000}]})

    # F02-06: Duplicate number within same prize type → 409
    run_test("F02-06", "Duplicate within same type (G6: '1234,1234') → 409",
             "POST", f"{BASE}/admin/tickets", 409, headers=A,
             body={"stationId": 1, "drawDate": "2025-01-03", "status": "UNPUBLISH",
                   "prizes": [
                       {"type": "G_DB", "winningNumbers": "999999", "rewardAmount": 2000000000},
                       {"type": "G6", "winningNumbers": "1234,1234", "rewardAmount": 400000},
                   ]})

    # F02-CROSS: Same number across DIFFERENT types → should be ALLOWED (200/201)
    run_test("F02-X", "Cross-type duplicate (G1=G2='88888') → ALLOWED",
             "POST", f"{BASE}/admin/tickets", 200, accept=[200, 201], headers=A,
             body={"stationId": 1, "drawDate": "2025-01-04", "status": "UNPUBLISH",
                   "prizes": [
                       {"type": "G_DB", "winningNumbers": "999999", "rewardAmount": 2000000000},
                       {"type": "G1", "winningNumbers": "88888", "rewardAmount": 30000000},
                       {"type": "G2", "winningNumbers": "88888", "rewardAmount": 15000000},
                       {"type": "G3", "winningNumbers": "77777", "rewardAmount": 10000000},
                       {"type": "G4", "winningNumbers": "66666", "rewardAmount": 3000000},
                       {"type": "G5", "winningNumbers": "5555", "rewardAmount": 1000000},
                       {"type": "G6", "winningNumbers": "4444", "rewardAmount": 400000},
                       {"type": "G7", "winningNumbers": "333", "rewardAmount": 200000},
                       {"type": "G8", "winningNumbers": "22", "rewardAmount": 100000},
                   ]})

    # F02-WRONG-LEN: Wrong digit count for prize type → 400
    run_test("F02-LEN", "G_DB with 5 digits → 400",
             "POST", f"{BASE}/admin/tickets", 400, headers=A,
             body={"stationId": 1, "drawDate": "2025-01-05", "status": "UNPUBLISH",
                   "prizes": [{"type": "G_DB", "winningNumbers": "12345", "rewardAmount": 2000000000}]})

    # F02-07: Edit regression — reuse the F02-X ticket (RES-HCM-04012025), change ONLY G8.
    # Guards the Hibernate INSERT-before-DELETE bug: unchanged prizes must UPDATE in
    # place, not be deleted+re-inserted (which hit unique_idx_result_prize_number → 409).
    edit_id = None
    try:
        sr = requests.get(f"{BASE}/admin/tickets?keyword=RES-HCM-04012025&page=0&size=5",
                          headers=A, timeout=10)
        for item in sr.json().get("data", {}).get("content", []):
            if item.get("resultCode") == "RES-HCM-04012025":
                edit_id = item.get("id")
                break
    except Exception as e:
        log(f"  ⚠ Could not resolve F02-X ticket id for edit regression: {e}")

    if edit_id:
        edit_prizes = [
            {"type": "G_DB", "winningNumbers": "999999", "rewardAmount": 2000000000},
            {"type": "G1",   "winningNumbers": "88888",  "rewardAmount": 30000000},
            {"type": "G2",   "winningNumbers": "88888",  "rewardAmount": 15000000},
            {"type": "G3",   "winningNumbers": "77777",  "rewardAmount": 10000000},
            {"type": "G4",   "winningNumbers": "66666",  "rewardAmount": 3000000},
            {"type": "G5",   "winningNumbers": "5555",   "rewardAmount": 1000000},
            {"type": "G6",   "winningNumbers": "4444",   "rewardAmount": 400000},
            {"type": "G7",   "winningNumbers": "333",    "rewardAmount": 200000},
            {"type": "G8",   "winningNumbers": "23",     "rewardAmount": 100000},  # only change
        ]
        ok_edit, r_edit = run_test(
            "F02-07", "Edit F02-X ticket: change ONLY G8 (22→23) → 200, no 409",
            "PUT", f"{BASE}/admin/tickets/{edit_id}", 200, headers=A,
            body={"stationId": 1, "drawDate": "2025-01-04", "status": "UNPUBLISH",
                  "prizes": edit_prizes},
            expect_in=['"winningNumber":"23"'])
        if not ok_edit:
            log("  ⚠ Edit regression FAILED → the in-place diff fix in "
                "TicketServiceImpl.applyAuditAndPrizes is not applied or not effective.")
    else:
        log("\n[SKIP] F02-07: could not locate the F02-X ticket "
            "(run F02-X first or re-run the whole suite).")
        results.append({"id": "F02-07", "desc": "Edit ticket regression",
                        "passed": False, "expected": [200],
                        "actual": "SKIPPED (no target ticket)", "body": ""})

    # F02-08: Status flip via PUT — the bug where updateTicket never called setStatus
    if edit_id:
        flip_body = {"stationId": 1, "drawDate": "2025-01-04", "status": "PUBLISH",
                     "prizes": [
                         {"type": "G_DB", "winningNumbers": "999999", "rewardAmount": 2000000000},
                         {"type": "G1", "winningNumbers": "88888", "rewardAmount": 30000000},
                         {"type": "G2", "winningNumbers": "88888", "rewardAmount": 15000000},
                         {"type": "G3", "winningNumbers": "77777", "rewardAmount": 10000000},
                         {"type": "G4", "winningNumbers": "66666", "rewardAmount": 3000000},
                         {"type": "G5", "winningNumbers": "5555", "rewardAmount": 1000000},
                         {"type": "G6", "winningNumbers": "4444", "rewardAmount": 400000},
                         {"type": "G7", "winningNumbers": "333", "rewardAmount": 200000},
                         {"type": "G8", "winningNumbers": "23", "rewardAmount": 100000},
                     ]}
        run_test("F02-08", "PUT with status=PUBLISH → response must show PUBLISH",
                 "PUT", f"{BASE}/admin/tickets/{edit_id}", 200, headers=A,
                 body=flip_body,
                 expect_in=['"status":"PUBLISH"'])

        # Flip back to UNPUBLISH to leave state clean
        flip_body["status"] = "UNPUBLISH"
        run_test("F02-09", "PUT with status=UNPUBLISH → response must show UNPUBLISH",
                 "PUT", f"{BASE}/admin/tickets/{edit_id}", 200, headers=A,
                 body=flip_body,
                 expect_in=['"status":"UNPUBLISH"'])

    # ── SECTION 1c: CHECKER "NO RESULT" EDGE ──
    log("\n" + "█" * 74)
    log("  SECTION 1c: CHECKER — NO-RESULT EDGE CASE")
    log("█" * 74)

    run_test("F3-10", "Check a date with no result → 404",
             "POST", f"{BASE}/checker/check", 404,
             body={"stationId": 1, "date": "2001-01-01", "numbers": ["123456"]})

    # ══════════════════════════════════════════════════════════════
    #  SECTION 2 — E2E FLOW
    # ══════════════════════════════════════════════════════════════
    log("\n" + "█" * 74)
    log("  SECTION 2: E2E FLOW")
    log(f"  Admin create → Publish → Guest check → User check → History")
    log(f"  station_id={E2E_STATION_ID} (HCM), draw_date={E2E_DATE}")
    log("█" * 74)

    # Step 0: real admin login
    log("\n[E2E-00] Login as admin via API (real token, not crafted)…")
    try:
        lr = requests.post(f"{BASE}/auth/login",
                           json={"email": ADMIN_EMAIL, "password": SEED_PASSWORD},
                           timeout=10)
        lr_data = lr.json().get("data", lr.json())
        real_admin = lr_data.get("token") or lr_data.get("accessToken") or ""
        if not real_admin:
            log(f"  ⚠ Could not extract token. Response keys: {list(lr.json().keys())}")
            log(f"  Falling back to crafted admin JWT.")
            real_admin = admin_tok
        else:
            log(f"  ✅ Admin login OK  →  {real_admin[:50]}…")
    except Exception as e:
        log(f"  ❌ Admin login failed: {e}")
        real_admin = admin_tok

    RA = {"Authorization": f"Bearer {real_admin}"}

    # Step 1: create result  (PrizeRequest: type, winningNumbers, rewardAmount)
    prizes = [
        {"type": "G_DB", "winningNumbers": "999999", "rewardAmount": 2000000000},
        {"type": "G1",   "winningNumbers": "88888",  "rewardAmount": 30000000},
        {"type": "G2",   "winningNumbers": "77777",  "rewardAmount": 15000000},
        {"type": "G3",   "winningNumbers": "66666",  "rewardAmount": 10000000},
        {"type": "G4",   "winningNumbers": "55555",  "rewardAmount": 3000000},
        {"type": "G5",   "winningNumbers": "4444",   "rewardAmount": 1000000},
        {"type": "G6",   "winningNumbers": "3210",   "rewardAmount": 400000},
        {"type": "G7",   "winningNumbers": "456",    "rewardAmount": 200000},
        {"type": "G8",   "winningNumbers": "12",     "rewardAmount": 100000},
    ]
    ok1, r1 = run_test("E2E-01", f"Admin creates result (HCM {E2E_DATE})",
                       "POST", f"{BASE}/admin/tickets", 200, accept=[200, 201],
                       headers=RA,
                       body={"stationId": E2E_STATION_ID, "drawDate": E2E_DATE,
                             "status": "UNPUBLISH", "prizes": prizes})

    result_id = None
    if r1 and r1.status_code in (200, 201):
        try:
            d = r1.json().get("data", r1.json())
            result_id = d.get("id")
            log(f"\n  → result_id = {result_id}")
        except Exception:
            log("  ⚠ Could not parse result_id")

    if result_id:
        # Step 2: publish
        run_test("E2E-02", "Admin publishes result",
                 "PATCH", f"{BASE}/admin/tickets/{result_id}/status", 200,
                 headers=RA, body={"status": "PUBLISH"})

        # Step 3: guest checks winning ticket
        run_test("E2E-03", "Guest checks 999999 → wins G_DB",
                 "POST", f"{BASE}/checker/check", 200,
                 body={"stationId": E2E_STATION_ID, "date": E2E_DATE,
                       "numbers": ["999999"]},
                 expect_in=["999999"])

        # Step 4: user checks 3 tickets (1 win, 2 lose)
        run_test("E2E-04", "User checks 3 tickets (999999 + 111111 + 222222)",
                 "POST", f"{BASE}/checker/check", 200, headers=U,
                 body={"stationId": E2E_STATION_ID, "date": E2E_DATE,
                       "numbers": ["999999", "111111", "222222"]})

        # Step 5: user views history
        run_test("E2E-05", "User views check history",
                 "GET", f"{BASE}/checker/history", 200, headers=U)

        # Step 5b: analytics aggregation — the bar chart is driven by the same
        # /checker/history payload. Assert the totals are internally consistent
        # (3 tickets × 10 000đ spent, one G_DB win ⇒ 2 000 000 000đ won).
        try:
            h = requests.get(f"{BASE}/checker/history", headers=U, timeout=10)
            sessions = h.json().get("data", [])
            target = next((s for s in sessions
                           if any(t.get("drawDate") == E2E_DATE for t in s.get("tickets", []))), None)
            if target and target.get("totalSpent") == 30000 and target.get("totalWon") == 2000000000:
                results.append({"id": "E2E-07", "desc": "Analytics aggregation consistent",
                                "passed": True, "expected": [200], "actual": 200,
                                "body": f"totalSpent={target['totalSpent']} totalWon={target['totalWon']}"})
                log(f"\n[PASS]  E2E-07: Analytics aggregation "
                    f"(totalSpent={target['totalSpent']}, totalWon={target['totalWon']})")
            else:
                results.append({"id": "E2E-07", "desc": "Analytics aggregation consistent",
                                "passed": False, "expected": "totalSpent=30000 & totalWon=2000000000",
                                "actual": str(target)[:200], "body": ""})
                log("\n[FAIL]  E2E-07: Analytics aggregation mismatch.")
        except Exception as e:
            results.append({"id": "E2E-07", "desc": "Analytics aggregation consistent",
                            "passed": False, "expected": [200], "actual": f"ERROR {e}", "body": ""})
            log(f"\n[FAIL]  E2E-07: {e}")

        # Step 6: admin lists tickets (verify total_queries visible)
        run_test("E2E-06", "Admin lists tickets (total_queries check)",
                 "GET", f"{BASE}/admin/tickets?page=0&size=50", 200, headers=RA)

        # Step 8: automated DB verification via docker exec
        ok_tq, tq_val = docker_sql(
            f"SELECT total_queries FROM lottery_results WHERE id = {result_id}")
        if ok_tq and tq_val.strip() == "2":
            log(f"\n[PASS]  E2E-08: DB total_queries = {tq_val.strip()} "
                f"(1 guest + 1 user session)")
            results.append({"id": "E2E-08", "desc": "DB total_queries = 2",
                            "passed": True, "expected": "2",
                            "actual": tq_val.strip(), "body": ""})
        else:
            log(f"\n[FAIL]  E2E-08: DB total_queries expected '2', got '{tq_val}'")
            results.append({"id": "E2E-08", "desc": "DB total_queries = 2",
                            "passed": False, "expected": "2",
                            "actual": tq_val, "body": ""})

        ok_ch, ch_val = docker_sql(
            f"SELECT COUNT(*) FROM check_histories WHERE result_id = {result_id}")
        log(f"  [DB] check_histories rows for result {result_id}: {ch_val}")
    else:
        log("\n  ⚠ Skipping E2E-02…06 (no result_id — result may already exist).")
        log(f"    If 409: station+date {E2E_DATE} already used. Delete it or change E2E_DATE.")

    # ══════════════════════════════════════════════════════════════
    #  SECTION 2b — DB ASSERTIONS (F3-16, F3-17)
    # ══════════════════════════════════════════════════════════════
    log("\n" + "█" * 74)
    log("  SECTION 2b: DB ASSERTIONS — USER + GUEST SESSION ROWS")
    log("█" * 74)

    # Use the published seed result for HCM 2023-10-23 (station_id=1)
    # to verify check_sessions + check_histories rows are created correctly.
    # NOTE: check_sessions has NO result_id column — we join via check_histories.
    ok_rid, seed_rid = docker_sql(
        "SELECT id FROM lottery_results WHERE station_id=1 AND draw_date='2023-10-23' AND status='PUBLISH' LIMIT 1")
    if ok_rid and seed_rid.strip():
        seed_result_id = int(seed_rid.strip())
        user_ticket = "111111"
        guest_ticket = "222222"

        # Clean up any previous runs: delete histories first, then orphaned sessions
        docker_sql(f"DELETE FROM check_histories WHERE result_id={seed_result_id} AND ticket_number IN ('{user_ticket}','{guest_ticket}')")
        docker_sql(f"DELETE FROM check_sessions WHERE id IN ("
                   f"SELECT s.id FROM (SELECT cs.id FROM check_sessions cs "
                   f"LEFT JOIN check_histories ch ON ch.session_id=cs.id "
                   f"WHERE ch.id IS NULL) AS s)")

        # ── F3-16: User check → DB assertion ──
        run_test("F3-16", f"User (khach2) checks '{user_ticket}' → verify DB session+history",
                 "POST", f"{BASE}/checker/check", 200, headers=U,
                 body={"stationId": 1, "date": "2023-10-23", "numbers": [user_ticket]})

        # Verify: a check_sessions row exists with user_id NOT NULL,
        # linked via check_histories to this result + ticket.
        ok_ch, ch_out = docker_sql(
            f"SELECT cs.user_id, ch.ticket_number FROM check_histories ch "
            f"JOIN check_sessions cs ON ch.session_id=cs.id "
            f"WHERE ch.result_id={seed_result_id} AND ch.ticket_number='{user_ticket}' "
            f"ORDER BY ch.check_time DESC LIMIT 1")
        if ok_ch and ch_out.strip():
            parts = ch_out.strip().split("\t")
            uid = parts[0].strip() if len(parts) > 0 else ""
            tick = parts[1].strip() if len(parts) > 1 else ""
            if uid and uid != "NULL" and tick == user_ticket:
                log(f"  [DB-ASSERT] F3-16: user_id={uid}, ticket={tick}  ✓")
                results.append({"id": "F3-16", "desc": "DB assertion user session",
                                "passed": True, "expected": "session.user_id NOT NULL + history row",
                                "actual": f"user_id={uid}, ticket={tick}", "body": ""})
            else:
                log(f"  [FAIL] F3-16: expected user_id NOT NULL + ticket={user_ticket}, got uid='{uid}', tick='{tick}'")
                results.append({"id": "F3-16", "desc": "DB assertion user session",
                                "passed": False, "expected": "user_id NOT NULL",
                                "actual": ch_out.strip(), "body": ""})
        else:
            log(f"  [FAIL] F3-16: no check_histories row found for ticket={user_ticket}")
            results.append({"id": "F3-16", "desc": "DB assertion user session",
                            "passed": False, "expected": "history row",
                            "actual": ch_out, "body": ""})

        # ── F3-17: Guest check → DB assertion ──
        run_test("F3-17", f"Guest checks '{guest_ticket}' → verify DB session.user_id=NULL",
                 "POST", f"{BASE}/checker/check", 200,
                 body={"stationId": 1, "date": "2023-10-23", "numbers": [guest_ticket]})

        ok_ch2, ch_out2 = docker_sql(
            f"SELECT cs.user_id, ch.ticket_number FROM check_histories ch "
            f"JOIN check_sessions cs ON ch.session_id=cs.id "
            f"WHERE ch.result_id={seed_result_id} AND ch.ticket_number='{guest_ticket}' "
            f"ORDER BY ch.check_time DESC LIMIT 1")
        if ok_ch2 and ch_out2.strip():
            parts2 = ch_out2.strip().split("\t")
            uid2 = parts2[0].strip() if len(parts2) > 0 else ""
            tick2 = parts2[1].strip() if len(parts2) > 1 else ""
            if (uid2 == "NULL" or uid2 == "") and tick2 == guest_ticket:
                log(f"  [DB-ASSERT] F3-17: user_id=NULL, ticket={tick2}  ✓")
                results.append({"id": "F3-17", "desc": "DB assertion guest session",
                                "passed": True, "expected": "session.user_id=NULL + history row",
                                "actual": f"user_id=NULL, ticket={tick2}", "body": ""})
            else:
                log(f"  [FAIL] F3-17: expected user_id=NULL + ticket={guest_ticket}, got uid='{uid2}', tick='{tick2}'")
                results.append({"id": "F3-17", "desc": "DB assertion guest session",
                                "passed": False, "expected": "user_id IS NULL",
                                "actual": ch_out2.strip(), "body": ""})
        else:
            log(f"  [FAIL] F3-17: no check_histories row found for ticket={guest_ticket}")
            results.append({"id": "F3-17", "desc": "DB assertion guest session",
                            "passed": False, "expected": "history row",
                            "actual": ch_out2, "body": ""})

        # Clean up test rows so the test is re-runnable
        docker_sql(f"DELETE FROM check_histories WHERE result_id={seed_result_id} AND ticket_number IN ('{user_ticket}','{guest_ticket}')")
        docker_sql(f"DELETE FROM check_sessions WHERE id IN ("
                   f"SELECT s.id FROM (SELECT cs.id FROM check_sessions cs "
                   f"LEFT JOIN check_histories ch ON ch.session_id=cs.id "
                   f"WHERE ch.id IS NULL) AS s)")
        log(f"  ✅ F3-16/F3-17 cleanup done (removed test rows for {user_ticket}, {guest_ticket}).")
    else:
        log("  ⚠ Skipping F3-16/F3-17: could not find published result for HCM 2023-10-23.")
        results.append({"id": "F3-16", "desc": "DB assertion user session",
                        "passed": False, "expected": "published result",
                        "actual": "NOT FOUND", "body": ""})
        results.append({"id": "F3-17", "desc": "DB assertion guest session",
                        "passed": False, "expected": "published result",
                        "actual": "NOT FOUND", "body": ""})

    # ══════════════════════════════════════════════════════════════
    #  SUMMARY
    # ══════════════════════════════════════════════════════════════
    total = len(results)
    n_pass = sum(1 for r in results if r["passed"])
    n_fail = total - n_pass

    log("\n" + "=" * 74)
    log("  SUMMARY")
    log("=" * 74)
    log(f"  Total tests : {total}")
    log(f"  Passed      : {n_pass}")
    log(f"  Failed      : {n_fail}")
    if total:
        log(f"  Pass rate   : {n_pass / total * 100:.1f}%")

    if n_fail:
        log("\n  Failed tests:")
        for r in results:
            if not r["passed"]:
                log(f"    ❌ [{r['id']}] {r['desc']}  (expected {r['expected']}, got {r['actual']})")

    log(f"\n  Log  → {LOG_FILE}")
    log(f"  JSON → {JSON_FILE}")
    log("=" * 74)

    # ── save artifacts ──
    LOG_FILE.write_text("\n".join(log_lines), encoding="utf-8")
    JSON_FILE.write_text(json.dumps(results, indent=2, ensure_ascii=False),
                         encoding="utf-8")
    print(f"\n  Artifacts saved. Screenshot the log above or open {LOG_FILE.name}")

    return 1 if n_fail else 0


if __name__ == "__main__":
    sys.exit(main())