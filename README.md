# The Vault

Local-first MVP for managing family financial and legal assets.

## Local Apps

- Backend: `backend/vault`
- Frontend: `frontend`

## MongoDB Atlas

The backend reads its MongoDB connection from `MONGODB_URI`.

PowerShell:

```powershell
cd H:\Projects\the-vault\backend\vault
$env:MONGODB_URI="mongodb+srv://<username>:<password>@<cluster-host>/<database>?retryWrites=true&w=majority"
.\mvnw.cmd spring-boot:run
```

Use the Atlas connection string from **Connect > Drivers > Java**. Replace the password placeholder, choose a database name such as `the-vault`, and URL-encode special characters in the password.

## Frontend

```powershell
cd H:\Projects\the-vault\frontend
copy .env.example .env
cmd /c npm install
cmd /c npm run dev
```

The app runs at `http://127.0.0.1:5173` and expects the backend at `http://localhost:8080`. Do not open `frontend/index.html` directly; Vite needs to serve the React modules.

## File Storage Direction

Use Amazon S3 for file bytes and MongoDB for document metadata.

Implemented MVP flow:

1. Frontend asks backend for a pre-signed upload URL.
2. Backend creates an S3 object key and returns a short-lived PUT URL.
3. Frontend uploads the file directly to S3.
4. Backend stores metadata in MongoDB: owner, linked asset, file name, content type, S3 bucket/key, checksum, created date.

Backend local env:

```properties
AWS_REGION=ap-south-1
S3_BUCKET=<bucket-name>
AWS_ACCESS_KEY_ID=<access-key-id>
AWS_SECRET_ACCESS_KEY=<secret-access-key>
```

The S3 bucket needs CORS for local uploads:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT"],
    "AllowedOrigins": ["http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

Alternatives worth considering:

- Local filesystem storage for the first private demo only.
- MinIO for an S3-compatible local development setup.
- Cloudflare R2 if S3-style object storage is wanted with simpler egress pricing.

## Visual Direction

Good themes for this app:

- Soft Ledger: warm off-white, muted green, charcoal text, subtle gold accents.
- Quiet Trust: pale blue-gray, ink, sage, restrained cards.
- Family Archive: ivory, soft olive, dusty rose, deep graphite.

The current frontend starts with the Soft Ledger direction.
