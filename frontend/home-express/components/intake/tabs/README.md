# Intake Tabs - Developer Guide

The intake tabs power the multi-step booking creation flow at `/customer/bookings/create`. After the November 2025 simplification we now support three, and only three, intake experiences: manual entry, AI image upload and paste-text parsing. This document explains the remaining tabs and how to work with them.

## Current Intake Options

1. **EnhancedManualTab (`enhanced-manual-tab.tsx`)** – structured form entry with advanced metadata controls.
2. **ImageUploadTab (`image-upload-tab.tsx`)** – drag & drop images for AI-powered object detection.
3. **PasteTextTab (`paste-text-tab.tsx`)** – let users paste/export lists of items and run them through the text parser service.

All tabs emit `ItemCandidate[]` objects via the shared `onAddCandidates` callback expected by `IntakeCollector`.

### EnhancedManualTab

**Purpose**
- Provide the most precise way to capture item details when AI extraction is not available or the user prefers manual control.

**Highlights**
- Brand/model autocomplete with fuzzy search.
- Category selector tied to the catalog API.
- Size + weight inputs with validation helpers.
- Fragile / disassembly / packaging flags.
- Batch add via "Add another" controls.

**Example**
```tsx
<EnhancedManualTab
  onAddCandidates={(items) => console.log(items)}
  submitButtonText="Thêm đồ"
/>
```

### ImageUploadTab

**Purpose**
- Let customers upload up to 10 photos and run them through `/api/v1/intake/analyze-images` for object detection.

**Highlights**
- Drag & drop or file picker input with size/type validation (JPG/PNG/WebP ≤ 10 MB each).
- Progress UI that mirrors the API batch status stream.
- Displays detected candidates with confidence and quick-edit affordances before emitting them upstream.

**Example**
```tsx
<ImageUploadTab
  sessionId={sessionId}
  onAddCandidates={setCandidates}
/>
```

### PasteTextTab

**Purpose**
- Convert pasted shopping lists, spreadsheet exports or Messenger texts into structured items through the text parsing service (`apiClient.parseText`).

**Highlights**
- Multi-line textarea with monospace styling for readability.
- Parser metadata badge (AI vs. rule-based) and confidence tags per candidate.
- Inline editing for quantity, fragile flags and declared value before accepting results.

**Example**
```tsx
<PasteTextTab
  onAddCandidates={(items) => console.log(items)}
/>
```

## Removed / Deprecated Tabs

The following tabs were removed from the UI and build to simplify the booking experience (request #HE-3981, November 2025):

- **CameraScanTab** – live camera capture for AI detection. Removed entirely (component deleted) and permissions flow retired.
- **OCRTab** – camera + upload hybrid for text extraction. Removed to avoid duplicate flows; use PasteTextTab instead.
- **DocumentUploadTab** – DOC/PDF/XLS ingestion. Deferred until we have a dedicated document pipeline.

If you need to reintroduce any of these, restore them from git history and coordinate with Product first.

## Integration Notes

- `IntakeCollector` now renders exactly three tabs. Consumers should not rely on other tab IDs.
- `onAddCandidates` is debounced only when `hideFooter=true`; if you embed a tab elsewhere, pass your own callbacks.
- Always keep the emitted objects aligned with `ItemCandidate` (see `types/index.d.ts`).

## Testing Checklist

### Manual Tab
- [ ] Add single and multiple items successfully.
- [ ] Validation prevents empty names or zero quantity.
- [ ] Flags (fragile/disassembly) persist after edit mode toggle.

### Image Upload Tab
- [ ] Upload guard rails (type, size, max count) block invalid files.
- [ ] Progress indicators match API responses.
- [ ] Cancelling midway cleans up `URL.createObjectURL` handles.

### Paste Text Tab
- [ ] Parser handles Vietnamese + English lists.
- [ ] Editing a parsed row updates the emitted candidate.
- [ ] "Add all" clears the textarea and result list afterward.

## Troubleshooting

| Issue | Likely Cause | Fix |
| --- | --- | --- |
| Images rejected immediately | Wrong mime type or >10 MB | Surface validation message from `validateFiles` helper |
| No candidates returned from parser | Parser service offline | Check `apiClient.parseText` response + server logs |
| Duplicate items after edit | Component consumer re-renders without key | Ensure parent list keys use `candidate.id` |

## References

- Booking workflow: `docs/MAIN_WORKFLOW.md`
- Requirements context: `docs/USER_REQUIREMENTS.md`
- Backend intake APIs: `backend/home-express-api` -> `IntakeTextParsingService`
