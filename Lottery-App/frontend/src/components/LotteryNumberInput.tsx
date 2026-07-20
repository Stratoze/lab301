import React, { useState, useEffect, useRef } from 'react';
import { Input, Tag, Space, message } from 'antd';
import { EditOutlined } from '@ant-design/icons';

interface LotteryNumberInputProps {
  chunkSize: number;         // digits per chunk (e.g., 5 for 5?digit numbers)
  maxChunks: number;         // 1 = single entry, >1 = multi?entry with badges
  value?: string;            // external comma/newline separated value
  onChange?: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

const LotteryNumberInput: React.FC<LotteryNumberInputProps> = ({
  chunkSize,
  maxChunks,
  value = '',
  onChange,
  placeholder,
  disabled = false,
}) => {
  const [chunks, setChunks] = useState<string[]>([]);
  const [currentInput, setCurrentInput] = useState('');
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const inputRef = useRef<any>(null);

  // Sync external value to internal chunks on mount and when value prop changes
  useEffect(() => {
    if (value === undefined) return;
    const parsed = value
      .split(/[\n,]+/)
      .map(s => s.trim())
      .filter(s => /^\d+$/.test(s));
    setChunks(parsed.slice(0, maxChunks));
    setCurrentInput('');
    setEditingIndex(null);
  }, [value, maxChunks]);

  const emitChange = (newChunks: string[]) => {
    const joined = newChunks.join(',');
    onChange?.(joined);
  };

  const isDuplicate = (candidate: string, excludeIndex?: number) => {
    return chunks.some((chunk, i) => i !== excludeIndex && chunk === candidate);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/\D/g, ''); // only digits
    if (editingIndex !== null) {
      // Editing an existing chunk
      if (raw.length <= chunkSize) {
        setCurrentInput(raw);
      }
      return;
    }
    // Normal entry
    if (maxChunks === 1) {
      // Single chunk: just limit to chunkSize
      setCurrentInput(raw.slice(0, chunkSize));
      return;
    }
    // Multi-chunk
    if (raw.length >= chunkSize) {
      // Split into completed chunks
      const newChunks: string[] = [];
      let remaining = raw;
      while (remaining.length >= chunkSize && newChunks.length + chunks.length < maxChunks) {
        newChunks.push(remaining.slice(0, chunkSize));
        remaining = remaining.slice(chunkSize);
      }
      if (newChunks.length > 0) {
        const deduped: string[] = [];
        for (const c of newChunks) {
          if (isDuplicate(c)) {
            message.warning(`Duplicate number "${c}" ignored.`);
          } else {
            deduped.push(c);
          }
        }
        if (deduped.length > 0) {
          const updated = [...chunks, ...deduped];
          setChunks(updated);
          emitChange(updated);
        }
        setCurrentInput(remaining); // leftover (or empty) becomes new input
        return;
      }
    }
    // Not yet complete chunk: just update
    setCurrentInput(raw);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // If user types comma or Enter, commit current input as chunk (if valid)
    if ((e.key === ',' || e.key === 'Enter') && maxChunks > 1 && editingIndex === null) {
      e.preventDefault();
      const trimmed = currentInput.trim();
      if (trimmed.length === chunkSize && /^\d+$/.test(trimmed)) {
        if (isDuplicate(trimmed)) {
          message.warning(`Duplicate number "${trimmed}" ignored.`);
          setCurrentInput('');
          return;
        }
        if (chunks.length < maxChunks) {
          const updated = [...chunks, trimmed];
          setChunks(updated);
          emitChange(updated);
          setCurrentInput('');
        }
      }
    }
    // Backspace on empty input removes last chunk
    if (e.key === 'Backspace' && currentInput === '' && chunks.length > 0 && editingIndex === null && maxChunks > 1) {
      const updated = chunks.slice(0, -1);
      setChunks(updated);
      emitChange(updated);
    }
    // Escape cancels editing
    if (e.key === 'Escape' && editingIndex !== null) {
      setEditingIndex(null);
      setCurrentInput('');
    }
    // Enter to confirm edit
    if (e.key === 'Enter' && editingIndex !== null) {
      e.preventDefault();
      if (currentInput.length === chunkSize && /^\d+$/.test(currentInput)) {
        const updated = [...chunks];
        updated[editingIndex] = currentInput;
        setChunks(updated);
        emitChange(updated);
        setEditingIndex(null);
        setCurrentInput('');
      }
    }
  };

  const handleDeleteChunk = (index: number) => {
    const updated = chunks.filter((_, i) => i !== index);
    setChunks(updated);
    emitChange(updated);
  };

  const handleEditChunk = (index: number) => {
    setEditingIndex(index);
    setCurrentInput(chunks[index]);
    // Focus input after state update
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const canAddMore = maxChunks === 1 ? chunks.length === 0 : chunks.length < maxChunks;

  return (
    <div>
      <Space wrap size={[4, 4]} style={{ marginBottom: maxChunks > 1 ? 8 : 0 }}>
        {chunks.map((chunk, idx) => (
          <Tag
            key={idx}
            closable={!disabled && maxChunks > 1}
            onClose={(e) => {
              e.preventDefault();
              handleDeleteChunk(idx);
            }}
            icon={maxChunks > 1 ? <EditOutlined onClick={() => handleEditChunk(idx)} style={{ cursor: 'pointer' }} /> : undefined}
            color={editingIndex === idx ? 'blue' : 'default'}
            style={{ fontSize: 14, padding: '2px 8px' }}
          >
            {chunk}
          </Tag>
        ))}
      </Space>
      {canAddMore && editingIndex === null && (
        <Input
          ref={inputRef}
          value={currentInput}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          placeholder={placeholder || `${chunkSize} digits`}
          maxLength={maxChunks === 1 ? chunkSize : undefined}
          disabled={disabled}
          style={{ borderRadius: 2 }}
        />
      )}
      {editingIndex !== null && (
        <Input
          ref={inputRef}
          value={currentInput}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          placeholder={`Edit ${chunkSize}-digit number`}
          maxLength={chunkSize}
          disabled={disabled}
          style={{ borderRadius: 2, marginTop: 4 }}
        />
      )}
    </div>
  );
};

export default LotteryNumberInput;