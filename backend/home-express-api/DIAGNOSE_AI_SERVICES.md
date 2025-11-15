# AI Services Diagnostic Guide

## Issue: Image Upload Detection Not Working

**Symptom:** Image upload keeps returning "không xác định được vật phẩm" (cannot identify items)

## Root Cause Analysis

Based on the code review, here are the most likely causes:

### 1. **Missing or Invalid AI API Keys** (MOST LIKELY)

The backend requires a valid OpenAI API key to be configured.

**Check your `.env` file in `backend/home-express-api/`:**

```bash
# Required for furniture detection (primary)
OPENAI_API_KEY=sk-proj-...your-actual-key...


```

**Verification Steps:**

1. **Check if `.env` file exists:**
   ```bash
   cd backend/home-express-api
   ls -la .env
   ```

2. **Check if OpenAI API key is set:**
   ```bash
   grep OPENAI_API_KEY .env
   ```
   - Should show: `OPENAI_API_KEY=sk-proj-...` (not empty)
   - If empty or missing, the AI detection will fail

### 2. **Backend Not Running or Wrong Port**

**Check if backend is running:**
```bash
# Check if backend is running on port 8084
curl http://localhost:8084/actuator/health

# Or check the process
netstat -ano | findstr :8084
```

**Expected:** Backend should be running on port 8084 (default)

### 3. **AI Service Configuration Issues**

**Check application.properties:**

The backend uses these configuration values:

```properties
# AI Detection Strategy
ai.detection.strategy=furniture
ai.detection.primary-service=openai
ai.detection.confidence-threshold=0.85

# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY:}
openai.model=gpt-5-mini
openai.api.url=https://api.openai.com/v1

### 4. **Backend Logs Show the Real Error**

**Check backend logs:**

```bash
cd backend/home-express-api
tail -f logs/application.log
```

**Look for these error patterns:**

```
✗ gpt-5-mini failed: API key not configured
✗ OpenAI Vision request failed: see logs
🚨 All AI services failed - Manual input required
```

### 5. **Network/CORS Issues**

**Check browser console:**
- Open DevTools (F12)
- Go to Network tab
- Try uploading an image
- Look for failed requests to `/api/v1/intake/analyze-images`

**Common errors:**
- `401 Unauthorized` - Authentication issue
- `500 Internal Server Error` - Backend AI service failure
- `CORS error` - CORS configuration issue

## Step-by-Step Diagnostic Process

### Step 1: Verify Environment Configuration

```bash
cd F:/01_Development/Active/home-express-project/backend/home-express-api

# Check if .env exists
if exist .env (
    echo .env file found
    type .env | findstr /C:"OPENAI_API_KEY" /C:"GOOGLE_CLOUD"
) else (
    echo ERROR: .env file not found!
    echo Please copy .env.example to .env and configure it
)
```

### Step 2: Check Backend Logs

```bash
# Start backend with verbose logging
cd backend/home-express-api
mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.homeexpress=DEBUG"
```

**Watch for:**
```
INFO  - 🎯 Using strategy: furniture | Primary service: openai
INFO  - 🪑 Furniture Detection: Using gpt-5-mini (primary) for 1 images...
✓ gpt-5-mini completed - Confidence: 95.00% - Latency: 2500ms - Items: 3
```

**Or errors:**
```
ERROR - ✗ gpt-5-mini failed: API key not configured
ERROR - ? OpenAI Vision request failed: check API key
ERROR - 🚨 All AI services failed - Reason: ALL_SERVICES_FAILED
```

### Step 3: Test AI Services Directly

**Create a test request:**

```bash
# Test with curl (replace with actual JWT token)
curl -X POST http://localhost:8084/api/v1/intake/analyze-images \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "images=@test-image.jpg"
```

**Expected response (success):**
```json
{
  "success": true,
  "data": {
    "candidates": [
      {
        "id": "...",
        "name": "Ghế sofa",
        "confidence": 0.95,
        "quantity": 1
      }
    ],
    "metadata": {
      "serviceUsed": "OPENAI_VISION",
      "confidence": 0.95,
      "processingTimeMs": 2500
    }
  }
}
```

**Expected response (failure - no API key):**
```json
{
  "success": false,
  "message": "Failed to analyze images: API key not configured"
}
```

### Step 4: Check Frontend API Call

**Open browser DevTools:**
1. Go to `http://localhost:3000/customer/bookings/create`
2. Open DevTools (F12) → Network tab
3. Upload an image
4. Look for request to `/api/v1/intake/analyze-images`

**Check request:**
- Method: POST
- Content-Type: multipart/form-data
- Body: Should contain image files

**Check response:**
- Status: 200 OK (success) or 500 (error)
- Body: Should contain `candidates` array

## Solutions

### Solution 1: Configure OpenAI API Key (RECOMMENDED)

1. **Get OpenAI API key:**
   - Go to https://platform.openai.com/api-keys
   - Create a new API key
   - Copy the key (starts with `sk-proj-...`)

2. **Add to `.env` file:**
   ```bash
   cd backend/home-express-api
   
   # Edit .env file
   notepad .env
   
   # Add this line:
   OPENAI_API_KEY=sk-proj-YOUR-ACTUAL-KEY-HERE
   ```

3. **Restart backend:**
   ```bash
   # Stop backend (Ctrl+C)
   # Start again
   mvnw spring-boot:run
   ```

4. **Test again:**
   - Upload an image in the frontend
   - Should now detect items successfully

### Solution 3: Check Backend Logs for Specific Errors

```bash
cd backend/home-express-api

# View recent logs
type logs\application.log | findstr /C:"ERROR" /C:"WARN" /C:"AI"

# Or tail logs in real-time
powershell Get-Content logs\application.log -Wait -Tail 50
```

**Common errors and fixes:**

| Error                        | Cause                             | Fix                            |
| ---------------------------- | --------------------------------- | ------------------------------ |
| `API key not configured`     | Missing OPENAI_API_KEY            | Add key to .env                |
| `401 Unauthorized`           | Invalid API key                   | Check key is correct           |
| `429 Too Many Requests`      | Rate limit exceeded               | Wait or upgrade plan           |
| `Budget exhausted`           | Budget limit reached              | Increase budget limits in .env |

### Solution 4: Temporary Workaround (Manual Entry)

If AI services cannot be configured immediately:

1. Use the "Nhập thủ công" (Manual Entry) tab instead
2. Manually enter item details
3. Configure AI services later for automated detection

## Verification Checklist

After applying fixes, verify:

- [ ] `.env` file exists with valid API keys
- [ ] Backend starts without errors
- [ ] Backend logs show: `? gpt-5-mini completed`
- [ ] Frontend image upload returns detected items
- [ ] No errors in browser console
- [ ] Items appear in the candidates list

## Additional Debugging

### Enable Debug Logging

**Edit `.env`:**
```bash
LOGGING_LEVEL_COM_HOMEEXPRESS=DEBUG
LOGGING_LEVEL_ROOT=INFO
```

**Restart backend and check logs:**
```bash
mvnw spring-boot:run
```

### Test Individual AI Services

**Check if GPTVisionService is configured:**
```java
// Look for this log on startup:
INFO  - GPTVisionService initialized with model: gpt-5-mini
```

java
// Look for this log on startup:

```

### Check Redis Cache

Sometimes cached results can cause issues:

```bash
# Connect to Redis
redis-cli

# Clear AI detection cache
FLUSHDB

# Exit
exit
```

## Contact Support

If issues persist after following this guide:

1. **Collect diagnostic information:**
   - Backend logs (last 100 lines)
   - Frontend browser console errors
   - Network tab showing failed requests
   - `.env` file (with API keys redacted)

2. **Check documentation:**
   - `docs/AI_FEATURES_BOOKING_CREATION.md`
   - `.env.example` for configuration reference

3. **Common issues:**
   - API key format incorrect (should start with `sk-proj-` for OpenAI)
   - Credentials file path incorrect (use relative path `./home-express-vision/...`)
   - Backend not restarted after configuration changes
   - CORS issues (check `app.cors.allowed-origins` in application.properties)

## Quick Fix Summary

**Most common issue: Missing OpenAI API key**

```bash
# 1. Edit .env
cd backend/home-express-api
notepad .env

# 2. Add this line (replace with your actual key):
OPENAI_API_KEY=sk-proj-YOUR-KEY-HERE

# 3. Restart backend
mvnw spring-boot:run

# 4. Test in frontend
# Upload an image → Should now work!
```

**Expected result:**
- Backend logs: `✓ gpt-5-mini completed - Confidence: 95.00%`
- Frontend: Shows detected items with confidence scores
- No more "không xác định được vật phẩm" error











