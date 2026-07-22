import os
import sys
import time
import subprocess
import platform
import csv

# Force stdout/stderr to handle UTF-8 cleanly on Windows terminals
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

# Configuration
PROJECT_ROOT = os.getcwd()
FRONTEND_DIR = os.path.join(PROJECT_ROOT, "Lottery-App", "frontend")
BACKEND_DIR = os.path.join(PROJECT_ROOT, "Lottery-App", "backend", "checker")

def copy_to_clipboard(text):
    """Copies text to system clipboard using native commands."""
    sys_os = platform.system()
    try:
        if sys_os == 'Windows':
            subprocess.run(['clip'], input=text, text=True, check=True)
        elif sys_os == 'Darwin':
            subprocess.run(['pbcopy'], input=text, text=True, check=True)
        elif sys_os == 'Linux':
            subprocess.run(['xclip', '-selection', 'clipboard'], input=text, text=True)
    except Exception:
        pass # Fail silently in headless/CI environments

def extract_errors(output, step_name):
    """Filters the raw output for error keywords and grabs log context."""
    error_keywords = ['[ERROR]', 'ERR!', 'FAIL', 'Exception:', 'Traceback', 'error']
    lines = output.splitlines()
    
    error_lines = []
    for line in lines:
        if any(kw in line for kw in error_keywords):
            error_lines.append(line.strip())
    
    # Remove duplicates but preserve order
    error_lines = list(dict.fromkeys(error_lines))
    
    summary = f"================ [FAILURE: {step_name}] ================\n\n"
    summary += "--- FILTERED ERRORS ---\n"
    summary += "\n".join(error_lines) if error_lines else "Could not auto-detect specific error lines."
    
    summary += "\n\n--- LAST 20 LINES OF LOG (Context) ---\n"
    summary += "\n".join(lines[-20:])
    
    return summary

def exec_step(step_name, cmd, cwd):
    """Executes shell command with real-time streaming and UTF-8 handling."""
    print(f"\n\033[96m========================================")
    print(f" ▶ {step_name}")
    print(f"========================================\033[0m")
    
    process = subprocess.Popen(
        cmd, 
        cwd=cwd, 
        shell=True, 
        stdout=subprocess.PIPE, 
        stderr=subprocess.STDOUT, 
        encoding='utf-8', 
        errors='replace'
    )
    
    captured_output = []
    for line in process.stdout:
        sys.stdout.write(line)
        sys.stdout.flush()
        captured_output.append(line)
        
    process.wait()
    full_output = "".join(captured_output)
    
    if process.returncode != 0:
        print(f"\033[91m❌ {step_name} FAILED! (Exit Code: {process.returncode})\033[0m")
        filtered_error = extract_errors(full_output, step_name)
        return False, filtered_error
        
    print(f"\033[92m✅ {step_name} PASSED!\033[0m")
    return True, None

def parse_jacoco_coverage(backend_dir):
    """Reads JaCoCo CSV report to compute total instruction coverage."""
    csv_path = os.path.join(backend_dir, "target", "site", "jacoco", "jacoco.csv")
    if not os.path.exists(csv_path):
        return None
        
    missed = 0
    covered = 0
    try:
        with open(csv_path, mode='r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                missed += int(row.get('INSTRUCTION_MISSED', 0))
                covered += int(row.get('INSTRUCTION_COVERED', 0))
                
        total = missed + covered
        if total == 0: return 0.0
        return (covered / total) * 100
    except Exception:
        return None

def main():
    start_time = time.time()
    captured_issues = []
    
    # ------------------------------------------------------------------
    # 1. FRONTEND
    # ------------------------------------------------------------------
    if os.path.exists(FRONTEND_DIR):
        if not os.path.exists(os.path.join(FRONTEND_DIR, "node_modules")):
            exec_step("Frontend: Install Dependencies", "npm install", FRONTEND_DIR)
            
        lint_ok, err = exec_step("Frontend: Lint", "npm run lint", FRONTEND_DIR)
        if not lint_ok: captured_issues.append(err)
        
        if lint_ok:
            build_ok, err = exec_step("Frontend: Build", "npm run build", FRONTEND_DIR)
            if not build_ok: captured_issues.append(err)
            
        test_ok, err = exec_step("Frontend: Unit Tests", "npm test", FRONTEND_DIR)
        if not test_ok: captured_issues.append(err)
    else:
        print(f"\033[93m⚠️ Frontend path not found: {FRONTEND_DIR}\033[0m")

    # ------------------------------------------------------------------
    # 2. BACKEND (Always runs regardless of Frontend failures)
    # ------------------------------------------------------------------
    if os.path.exists(BACKEND_DIR):
        mvn_cmd = "mvnw.cmd" if platform.system() == 'Windows' else "./mvnw"
        cmd = f"{mvn_cmd} clean verify jacoco:report -Dspotbugs.xmlOutput=false"
        
        backend_ok, err = exec_step("Backend: Static Analysis, Tests & JaCoCo", cmd, BACKEND_DIR)
        if not backend_ok:
            captured_issues.append(err)
        else:
            coverage = parse_jacoco_coverage(BACKEND_DIR)
            if coverage is not None:
                color = "\033[92m" if coverage > 80 else "\033[93m"
                print(f"\n{color}📊 JaCoCo Total Code Coverage: {coverage:.2f}%\033[0m")
    else:
        print(f"\033[93m⚠️ Backend path not found: {BACKEND_DIR}\033[0m")

    # ------------------------------------------------------------------
    # 3. SUMMARY
    # ------------------------------------------------------------------
    elapsed = time.strftime("%M:%S", time.gmtime(time.time() - start_time))
    print(f"\n\033[93m========================================")
    print(f" Elapsed Time: {elapsed}")
    
    if captured_issues:
        full_report = "\n\n".join(captured_issues)
        copy_to_clipboard(full_report)
        print(f"\033[91m❌ CHECKS FAILED!")
        print(f"📋 Smart error log copied to clipboard.\033[0m")
        sys.exit(1)
    else:
        copy_to_clipboard("") # Clear clipboard on success
        print(f"\033[92m🎉 ALL CHECKS PASSED!\033[0m")
        sys.exit(0)

if __name__ == "__main__":
    main()
