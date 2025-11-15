# Quy tắc Logging và Bảo mật

## 🚨 Nguyên tắc quan trọng

### ❌ KHÔNG BAO GIỜ log các thông tin sau ra console:

1. **Tokens và Credentials**
   - `accessToken`, `refreshToken`, `token`
   - `password`, `newPassword`, `currentPassword`
   - JWT tokens
   - API keys
   - Session IDs

2. **Thông tin cá nhân nhạy cảm (PII)**
   - Email addresses
   - Số điện thoại
   - Địa chỉ đầy đủ
   - CMND/CCCD
   - Thông tin tài khoản ngân hàng

3. **Response objects hoàn chỉnh từ API authentication**
   - Toàn bộ response từ `/auth/login`
   - Toàn bộ response từ `/auth/register`
   - Toàn bộ response từ `/auth/refresh`

## ✅ Sử dụng Logger thay vì Console

### Import và sử dụng logger

```typescript
import { logger } from "@/lib/logger"

// ✅ Đúng - Log với thông tin an toàn
logger.info("Login successful", { 
  userId: user.user_id,
  role: user.role 
})

// ❌ Sai - Log toàn bộ response
console.log("Login successful:", response) // Chứa token!
```

### Các phương thức logger có sẵn

- `logger.debug(message, context)` - Chỉ hiển thị trong development
- `logger.info(message, context)` - Thông tin chung
- `logger.warn(message, context)` - Cảnh báo
- `logger.error(message, context)` - Lỗi

### Ví dụ logging an toàn

```typescript
// ✅ Login attempt - không log email
logger.info("Login attempt initiated")

// ✅ Login success - chỉ log metadata
logger.info("Login successful", { 
  userId: response.user.user_id,
  role: response.user.role,
  hasAccessToken: !!response.accessToken  // Boolean, không phải giá trị thật
})

// ✅ Error handling - chỉ log message
logger.error("Login failed", { 
  message: error.message,
  code: error.code
})

// ✅ API calls - log performance, không log response
logger.info("API Call: /bookings", {
  duration: "234ms",
  status: 200
})
```

### Ví dụ logging KHÔNG an toàn

```typescript
// ❌ Log toàn bộ response (chứa token)
console.log("Login response:", response)

// ❌ Log email người dùng
console.log("Login attempt:", email)

// ❌ Log credentials
console.log("Authenticating:", { email, password })

// ❌ Log token trực tiếp
console.log("Access token:", accessToken)

// ❌ Log toàn bộ error object (có thể chứa sensitive headers)
console.error("Error:", error)
```

## 🛡️ ESLint Configuration

Dự án đã được cấu hình ESLint để cảnh báo khi sử dụng `console.log`:

```json
{
  "rules": {
    "no-console": ["warn", { "allow": ["warn", "error"] }]
  }
}
```

- `console.log` sẽ hiển thị warning
- `console.warn` và `console.error` được cho phép nhưng vẫn nên sử dụng `logger` thay thế

## 🔍 Kiểm tra trước khi commit

Trước khi commit code, hãy tự kiểm tra:

1. Mở browser DevTools → Console tab
2. Test luồng authentication (login/register)
3. Xác nhận KHÔNG có token/credentials hiển thị trong console
4. Chạy ESLint: `npm run lint`

## 📝 Development vs Production

Logger tự động phân biệt môi trường:

- **Development**: `logger.debug()` hiển thị chi tiết
- **Production**: Chỉ log từ `info` trở lên, và gửi lên monitoring service

**Lưu ý**: Dù trong development, KHÔNG BAO GIỜ log token/credentials!

## 🚀 Best Practices

1. **Luôn sử dụng `logger`** thay vì `console.*`
2. **Chỉ log metadata**, không log giá trị nhạy cảm
3. **Review code** trước khi tạo Pull Request
4. **Test thủ công** - Kiểm tra console trong browser
5. **Sử dụng TypeScript** - Type safety giúp tránh lỗi

## 📚 Tham khảo

- Logger implementation: `/lib/logger.ts`
- Auth context: `/contexts/auth-context.tsx`
- API Client: `/lib/api-client.ts`

## ❓ Khi nào cần debug với thông tin chi tiết?

Nếu thực sự cần debug với thông tin chi tiết (chỉ trong development local):

1. Sử dụng breakpoints trong DevTools thay vì console.log
2. Sử dụng `logger.debug()` với thông tin đã được sanitize
3. KHÔNG commit code debug vào repository

---

**Cập nhật lần cuối**: 2025-11-15
**Liên hệ**: Security Team
