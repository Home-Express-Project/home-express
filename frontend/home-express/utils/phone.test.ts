import { normalizeVNPhone, isValidVNPhone } from './phone';

describe('normalizeVNPhone', () => {
  it('should normalize international format with +84 prefix and spaces', () => {
    expect(normalizeVNPhone('+84 912 345 678')).toBe('0912345678');
  });

  it('should normalize international format with 84 prefix (no plus)', () => {
    expect(normalizeVNPhone('84912345678')).toBe('0912345678');
  });

  it('should keep already normalized phone numbers unchanged', () => {
    expect(normalizeVNPhone('0912345678')).toBe('0912345678');
  });

  it('should normalize phone with dashes', () => {
    expect(normalizeVNPhone('091-234-5678')).toBe('0912345678');
  });

  it('should normalize phone with dots', () => {
    expect(normalizeVNPhone('0912.345.678')).toBe('0912345678');
  });

  it('should normalize phone with parentheses', () => {
    expect(normalizeVNPhone('(091) 234-5678')).toBe('0912345678');
  });

  it('should normalize mixed format', () => {
    expect(normalizeVNPhone('+84 (91) 234-56.78')).toBe('0912345678');
  });

  it('should return empty string for empty input', () => {
    expect(normalizeVNPhone('')).toBe('');
  });

  it('should handle leading/trailing spaces', () => {
    expect(normalizeVNPhone('  0912345678  ')).toBe('0912345678');
  });

  it('should add leading 0 if missing (for domestic format)', () => {
    expect(normalizeVNPhone('912345678')).toBe('0912345678');
  });
});

describe('isValidVNPhone', () => {
  it('should validate correct 10-digit phone starting with 0', () => {
    expect(isValidVNPhone('0912345678')).toBe(true);
    expect(isValidVNPhone('0123456789')).toBe(true);
    expect(isValidVNPhone('0987654321')).toBe(true);
  });

  it('should reject phone with less than 10 digits', () => {
    expect(isValidVNPhone('091234567')).toBe(false);
    expect(isValidVNPhone('012345')).toBe(false);
  });

  it('should reject phone with more than 10 digits', () => {
    expect(isValidVNPhone('09123456789')).toBe(false);
    expect(isValidVNPhone('012345678901')).toBe(false);
  });

  it('should reject phone not starting with 0', () => {
    expect(isValidVNPhone('1912345678')).toBe(false);
    expect(isValidVNPhone('9123456789')).toBe(false);
  });

  it('should reject phone with non-digit characters', () => {
    expect(isValidVNPhone('091-234-5678')).toBe(false);
    expect(isValidVNPhone('+84912345678')).toBe(false);
    expect(isValidVNPhone('0912 345 678')).toBe(false);
  });

  it('should reject empty or null input', () => {
    expect(isValidVNPhone('')).toBe(false);
    expect(isValidVNPhone(null as any)).toBe(false);
  });
});

describe('Integration: normalize then validate', () => {
  it('should normalize and validate international format', () => {
    const normalized = normalizeVNPhone('+84 912 345 678');
    expect(isValidVNPhone(normalized)).toBe(true);
  });

  it('should normalize and validate domestic format with spaces', () => {
    const normalized = normalizeVNPhone('091 234 5678');
    expect(isValidVNPhone(normalized)).toBe(true);
  });

  it('should normalize but still fail validation for invalid length', () => {
    const normalized = normalizeVNPhone('091234567'); // Only 9 digits
    expect(isValidVNPhone(normalized)).toBe(false);
  });
});
