# The Vault

MVP for managing family financial and legal assets.

## Installation

### Prerequisites

- Java 21 or higher
- Maven
- Node.js and npm
- MongoDB Atlas account
- AWS account with S3 bucket configured for CORS (see below)

### Backend Setup

1. Create a MongoDB Atlas cluster and obtain the connection string from **Connect > Drivers > Java**.
2. Set the following environment variables in your PowerShell session:
   ```
   $env:MONGODB_URI="mongodb+srv://<username>:<password>@<cluster-host>/<database>?retryWrites=true&w=majority"
   $env:AWS_REGION="ap-south-1"
   $env:S3_BUCKET="<bucket-name>"
   $env:AWS_ACCESS_KEY_ID="<access-key-id>"
   $env:AWS_SECRET_ACCESS_KEY="<secret-access-key>"
   ```
   Replace placeholders with your actual values. URL-encode special characters in the password.
3. Run the backend:
   ```
   cd H:\Projects\the-vault\backend\vault
   .\mvnw.cmd spring-boot:run
   ```

### Frontend Setup

1. Navigate to the frontend directory:
   ```
   cd H:\Projects\the-vault\frontend
   ```
2. Copy the example environment file:
   ```
   copy .env.example .env
   ```
3. Install dependencies:
   ```
   npm install
   ```
4. Start the development server:
   ```
   npm run dev
   ```

The frontend runs at `http://127.0.0.1:5173` and connects to the backend at `http://localhost:8080`. Do not open `frontend/index.html` directly; use the Vite dev server.

### S3 Bucket CORS Configuration

Configure your S3 bucket with the following CORS policy for local development:

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

## Usage

- Access the frontend to manage family financial and legal assets.
- Files are uploaded directly to S3, with metadata stored in MongoDB.
