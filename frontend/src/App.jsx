import { useEffect, useMemo, useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

const subtypeOptions = {
  IDENTITY: ['AADHAAR_CARD', 'PASSPORT', 'VOTER_ID', 'DRIVING_LICENSE', 'PAN_CARD'],
  BANK_ACCOUNT: ['SAVINGS_ACCOUNT', 'CURRENT_ACCOUNT', 'SALARY_ACCOUNT', 'BANK_LOCKER'],
  DEPOSIT: ['FIXED_DEPOSIT', 'RECURRING_DEPOSIT'],
  INVESTMENT: ['MUTUAL_FUND', 'STOCK', 'PPF'],
  INSURANCE: ['LIFE_INSURANCE', 'TERM_INSURANCE', 'HEALTH_INSURANCE', 'VEHICLE_INSURANCE', 'TRAVEL_INSURANCE'],
  PHYSICAL_ASSET: ['PROPERTY_DOCUMENT', 'VEHICLE_DOCUMENT', 'JEWELLERY', 'COMMODITY'],
  OTHER: ['OTHER'],
};

const dataFields = {
  IDENTITY: [
    { name: 'idNumber', label: 'ID number' },
    { name: 'issuedBy', label: 'Issued by' },
    { name: 'expiryDate', label: 'Expiry date', type: 'date' },
  ],
  BANK_ACCOUNT: [
    { name: 'bankName', label: 'Bank name' },
    { name: 'accountLast4', label: 'Account last 4 digits', maxLength: 4 },
    { name: 'ifsc', label: 'IFSC code' },
    { name: 'branch', label: 'Branch' },
  ],
  DEPOSIT: [
    { name: 'institution', label: 'Institution' },
    { name: 'depositNumber', label: 'Deposit number' },
    { name: 'maturityDate', label: 'Maturity date', type: 'date' },
    { name: 'amount', label: 'Amount', type: 'number' },
  ],
  INVESTMENT: [
    { name: 'provider', label: 'Provider' },
    { name: 'folioOrAccount', label: 'Folio / account' },
    { name: 'nominee', label: 'Nominee' },
  ],
  INSURANCE: [
    { name: 'provider', label: 'Provider' },
    { name: 'policyNumber', label: 'Policy number' },
    { name: 'sumAssured', label: 'Sum assured', type: 'number' },
    { name: 'renewalDate', label: 'Renewal date', type: 'date' },
  ],
  PHYSICAL_ASSET: [
    { name: 'location', label: 'Location' },
    { name: 'identifier', label: 'Identifier' },
    { name: 'estimatedValue', label: 'Estimated value', type: 'number' },
  ],
  OTHER: [
    { name: 'reference', label: 'Reference' },
    { name: 'notes', label: 'Notes' },
  ],
};

const initialForm = {
  ownerUserId: 'family-demo',
  assetType: 'BANK_ACCOUNT',
  subType: 'SAVINGS_ACCOUNT',
  title: '',
  description: '',
  data: {},
};

function labelize(value) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function compactData(data) {
  return Object.fromEntries(
    Object.entries(data).filter(([, value]) => value !== undefined && value !== null && `${value}`.trim() !== ''),
  );
}

function App() {
  const [health, setHealth] = useState('checking');
  const [assets, setAssets] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [selectedFile, setSelectedFile] = useState(null);
  const [linkedAssetId, setLinkedAssetId] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [uploadMessage, setUploadMessage] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteConfirmation, setDeleteConfirmation] = useState('');
  const [deleteMessage, setDeleteMessage] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);

  const visibleSubtypes = useMemo(() => subtypeOptions[form.assetType] ?? [], [form.assetType]);
  const visibleDataFields = useMemo(() => dataFields[form.assetType] ?? [], [form.assetType]);

  async function loadAssets(ownerUserId = form.ownerUserId) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/assets?ownerUserId=${encodeURIComponent(ownerUserId)}`);
      if (!response.ok) {
        throw new Error('Could not load assets');
      }
      setAssets(await response.json());
    } catch {
      setAssets([]);
    }
  }

  async function loadDocuments(ownerUserId = form.ownerUserId) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/documents?ownerUserId=${encodeURIComponent(ownerUserId)}`);
      if (!response.ok) {
        throw new Error('Could not load documents');
      }
      setDocuments(await response.json());
    } catch {
      setDocuments([]);
    }
  }

  useEffect(() => {
    async function boot() {
      try {
        const healthResponse = await fetch(`${API_BASE_URL}/health`);
        setHealth(healthResponse.ok ? 'online' : 'offline');
      } catch {
        setHealth('offline');
      }

      await loadAssets(initialForm.ownerUserId);
      await loadDocuments(initialForm.ownerUserId);
    }

    boot();
  }, []);

  function updateField(field, value) {
    setMessage('');
    setForm((current) => {
      const next = { ...current, [field]: value };
      if (field === 'assetType') {
        next.subType = subtypeOptions[value]?.[0] ?? '';
        next.data = {};
      }
      return next;
    });
  }

  function updateDataField(field, value) {
    setMessage('');
    setForm((current) => ({
      ...current,
      data: {
        ...current.data,
        [field]: value,
      },
    }));
  }

  async function createAsset(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/assets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ownerUserId: form.ownerUserId,
          assetType: form.assetType,
          subType: form.subType,
          title: form.title,
          description: form.description,
          data: compactData(form.data),
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message ?? 'Could not save asset');
      }

      const ownerUserId = form.ownerUserId;
      setForm({ ...initialForm, ownerUserId });
      setMessage('Asset saved');
      await loadAssets(ownerUserId);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setIsSaving(false);
    }
  }

  async function uploadDocument(event) {
    event.preventDefault();
    if (!selectedFile) {
      setUploadMessage('Choose a PDF or image first');
      return;
    }

    setIsUploading(true);
    setUploadMessage('');

    try {
      const uploadResponse = await fetch(`${API_BASE_URL}/api/documents/upload-url`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ownerUserId: form.ownerUserId,
          linkedAssetId: linkedAssetId || null,
          fileName: selectedFile.name,
          contentType: selectedFile.type || 'application/octet-stream',
          sizeBytes: selectedFile.size,
        }),
      });

      if (!uploadResponse.ok) {
        const error = await uploadResponse.json();
        throw new Error(error.message ?? 'Could not prepare upload');
      }

      const uploadPlan = await uploadResponse.json();
      const s3Response = await fetch(uploadPlan.uploadUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': selectedFile.type,
        },
        body: selectedFile,
      });

      if (!s3Response.ok) {
        throw new Error('S3 upload failed. Check bucket CORS and credentials.');
      }

      const completeResponse = await fetch(`${API_BASE_URL}/api/documents/${uploadPlan.document.id}/complete`, {
        method: 'POST',
      });

      if (!completeResponse.ok) {
        const error = await completeResponse.json();
        throw new Error(error.message ?? 'Upload finished, but verification failed');
      }

      setSelectedFile(null);
      setLinkedAssetId('');
      event.target.reset();
      setUploadMessage('Document uploaded');
      await loadDocuments(form.ownerUserId);
    } catch (error) {
      setUploadMessage(error.message);
    } finally {
      setIsUploading(false);
    }
  }

  function openDeleteModal(document) {
    setDeleteTarget(document);
    setDeleteConfirmation('');
    setDeleteMessage('');
  }

  function closeDeleteModal() {
    if (isDeleting) {
      return;
    }
    setDeleteTarget(null);
    setDeleteConfirmation('');
    setDeleteMessage('');
  }

  async function deleteDocument() {
    if (!deleteTarget || deleteConfirmation !== 'DELETE') {
      return;
    }

    setIsDeleting(true);
    setDeleteMessage('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/documents/${deleteTarget.id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message ?? 'Could not delete document');
      }

      closeDeleteModal();
      await loadDocuments(form.ownerUserId);
    } catch (error) {
      setDeleteMessage(error.message);
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">The Vault</p>
          <h1>Family assets</h1>
        </div>
        <div className={`status ${health}`}>
          <span />
          Backend {health}
        </div>
        <div className="summary">
          <strong>{assets.length}</strong>
          <span>saved assets</span>
        </div>
      </aside>

      <section className="workspace">
        <div className="left-column">
        <form className="asset-form" onSubmit={createAsset}>
          <div className="section-heading">
            <h2>Add asset</h2>
            <button type="submit" disabled={isSaving}>
              {isSaving ? 'Saving' : 'Save'}
            </button>
          </div>

          <div className="form-grid">
            <label>
              Owner
              <input value={form.ownerUserId} onChange={(event) => updateField('ownerUserId', event.target.value)} />
            </label>
            <label>
              Title
              <input value={form.title} onChange={(event) => updateField('title', event.target.value)} required />
            </label>
            <label>
              Type
              <select value={form.assetType} onChange={(event) => updateField('assetType', event.target.value)}>
                {Object.keys(subtypeOptions).map((type) => (
                  <option key={type} value={type}>{labelize(type)}</option>
                ))}
              </select>
            </label>
            <label>
              Subtype
              <select value={form.subType} onChange={(event) => updateField('subType', event.target.value)}>
                {visibleSubtypes.map((type) => (
                  <option key={type} value={type}>{labelize(type)}</option>
                ))}
              </select>
            </label>
          </div>

          <label>
            Description
            <input value={form.description} onChange={(event) => updateField('description', event.target.value)} />
          </label>

          <div className="field-group">
            <h3>{labelize(form.assetType)} details</h3>
            <div className="form-grid">
              {visibleDataFields.map((field) => (
                <label key={field.name}>
                  {field.label}
                  <input
                    type={field.type ?? 'text'}
                    maxLength={field.maxLength}
                    value={form.data[field.name] ?? ''}
                    onChange={(event) => updateDataField(field.name, event.target.value)}
                  />
                </label>
              ))}
            </div>
          </div>

          {message && <p className="message">{message}</p>}
        </form>

        <form className="asset-form" onSubmit={uploadDocument}>
          <div className="section-heading">
            <h2>Upload document</h2>
            <button type="submit" disabled={isUploading}>
              {isUploading ? 'Uploading' : 'Upload'}
            </button>
          </div>

          <label>
            Link to asset
            <select value={linkedAssetId} onChange={(event) => setLinkedAssetId(event.target.value)}>
              <option value="">No linked asset</option>
              {assets.map((asset) => (
                <option key={asset.id} value={asset.id}>{asset.title}</option>
              ))}
            </select>
          </label>

          <label>
            File
            <input
              type="file"
              accept="application/pdf,image/*"
              onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
            />
          </label>

          {selectedFile && (
            <p className="file-chip">
              {selectedFile.name} - {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
            </p>
          )}

          {uploadMessage && <p className="message">{uploadMessage}</p>}
        </form>
        </div>

        <div className="right-column">
        <section className="asset-list">
          <div className="section-heading">
            <h2>Assets</h2>
            <button type="button" className="secondary" onClick={() => {
              loadAssets();
              loadDocuments();
            }}>Refresh</button>
          </div>

          <div className="rows">
            {assets.map((asset) => (
              <article key={asset.id} className="asset-row">
                <div>
                  <strong>{asset.title}</strong>
                  <span>{labelize(asset.assetType)} / {asset.subType ? labelize(asset.subType) : 'No subtype'}</span>
                  {asset.description && <small>{asset.description}</small>}
                </div>
                <code>{asset.ownerUserId}</code>
              </article>
            ))}
            {assets.length === 0 && <p className="empty">No assets yet.</p>}
          </div>
        </section>

        <section className="asset-list">
          <div className="section-heading">
            <h2>Documents</h2>
          </div>

          <div className="rows">
            {documents.map((document) => (
              <article key={document.id} className="asset-row">
                <div>
                  <strong>{document.fileName}</strong>
                  <span>{document.contentType} / {document.status}</span>
                  {document.linkedAssetId && <small>Linked asset: {document.linkedAssetId}</small>}
                  {document.objectUrl && <small>{document.objectUrl}</small>}
                </div>
                <div className="row-actions">
                  <code>{document.sizeBytes ? `${(document.sizeBytes / 1024 / 1024).toFixed(2)} MB` : 'file'}</code>
                  {document.downloadUrl && (
                    <a className="link-button" href={document.downloadUrl} target="_blank" rel="noreferrer">
                      View
                    </a>
                  )}
                  <button type="button" className="danger-button" onClick={() => openDeleteModal(document)}>
                    Delete
                  </button>
                </div>
              </article>
            ))}
            {documents.length === 0 && <p className="empty">No documents yet.</p>}
          </div>
        </section>
        </div>
      </section>

      {deleteTarget && (
        <div className="modal-backdrop" role="presentation" onMouseDown={closeDeleteModal}>
          <section className="modal" role="dialog" aria-modal="true" aria-labelledby="delete-document-title" onMouseDown={(event) => event.stopPropagation()}>
            <h2 id="delete-document-title">Delete document</h2>
            <p>
              This removes the file from S3 and deletes its metadata from the vault.
            </p>
            <p className="modal-file-name">{deleteTarget.fileName}</p>
            <label>
              Type DELETE to confirm
              <input
                autoFocus
                value={deleteConfirmation}
                onChange={(event) => setDeleteConfirmation(event.target.value)}
              />
            </label>
            {deleteMessage && <p className="message">{deleteMessage}</p>}
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={closeDeleteModal} disabled={isDeleting}>
                Cancel
              </button>
              <button
                type="button"
                className="danger-button solid"
                disabled={deleteConfirmation !== 'DELETE' || isDeleting}
                onClick={deleteDocument}
              >
                {isDeleting ? 'Deleting' : 'Delete'}
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}

export default App;
