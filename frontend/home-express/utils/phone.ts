/**
 * Normalize Vietnamese phone numbers to the format required by the backend.
 * 
 * Rules:
 * - Strips all non-digit characters (spaces, dashes, parentheses, etc.)
 * - Converts international prefix 84 to local prefix 0
 * - Ensures the result starts with 0 and is exactly 10 digits
 * 
 * Examples:
 * - "+84 912 345 678" → "0912345678"
 * - "84912345678" → "0912345678"
 * - "0912345678" → "0912345678"
 * - "091-234-5678" → "0912345678"
 */
export const normalizeVNPhone = (input: string): string => {
  if (!input) return '';
  
  // Strip all non-digit characters
  const digits = input.replace(/[^\d]/g, '');
  
  if (!digits) return '';
  
  let normalized = digits;
  
  // Convert 84 prefix to 0 prefix
  if (normalized.startsWith('84')) {
    normalized = normalized.slice(2);
  }
  
  // Ensure it starts with 0
  if (!normalized.startsWith('0')) {
    normalized = '0' + normalized;
  }
  
  return normalized;
};

/**
 * Validate Vietnamese phone number format.
 * Must be exactly 10 digits starting with 0.
 */
export const isValidVNPhone = (input: string): boolean => {
  return /^0\d{9}$/.test(input);
};
