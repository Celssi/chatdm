# ChatDM MCP Server - Cloud Connection

Service URL: **https://chatdm-334017779319.europe-north1.run.app**

## Cursor Configuration

Add to `.cursor/mcp.json` (project) or `~/.cursor/mcp.json` (global):

### Option 1: SSE transport (try first)
```json
{
  "mcpServers": {
    "chatdm_cloud": {
      "url": "https://chatdm-334017779319.europe-north1.run.app",
      "transportType": "sse"
    }
  }
}
```

### Option 2: Streamable HTTP
```json
{
  "mcpServers": {
    "chatdm_cloud": {
      "url": "https://chatdm-334017779319.europe-north1.run.app/mcp",
      "transportType": "streamable-http"
    }
  }
}
```

### Option 3: Plain URL (let client auto-detect)
```json
{
  "mcpServers": {
    "chatdm_cloud": {
      "url": "https://chatdm-334017779319.europe-north1.run.app/mcp"
    }
  }
}
```

## Claude Desktop Configuration

Claude's Connectors UI only supports OAuth or authless servers. To use Wryterio tools, pass the token in the URL:

**Add via Settings > Connectors** using this URL (replace with your token):
```
https://chatdm-334017779319.europe-north1.run.app/mcp?wryterio_token=wrt_your_token_here
```

Or via CLI:
```bash
claude mcp add --transport http chatdm "https://chatdm-334017779319.europe-north1.run.app/mcp?wryterio_token=wrt_your_token_here"
```

> **Note:** Token in URL is less secure (may appear in logs). Prefer header auth when your client supports it (e.g. Cursor).

## Troubleshooting "handles auth correctly" Error

This error often means the client couldn't connect. Try:

1. **Restart Cursor/Claude** after changing MCP config
2. **Try different transport**: Switch between `sse` and `streamable-http`
3. **Check URL**: Ensure no trailing slash, use `/mcp` for Streamable HTTP
4. **Cold start**: First request may take 10-30s if the service was idle

The server allows unauthenticated access - no API key or auth headers needed.

## Novel Writing & Wryterio Integration

For novel-writing tools that fetch from [Wryterio](https://wryterio.com/), add your API token:

**Option A: Header** (Cursor, clients that support custom headers):
```json
{
  "mcpServers": {
    "chatdm_cloud": {
      "url": "https://chatdm-334017779319.europe-north1.run.app",
      "transportType": "sse",
      "headers": {
        "X-Wryterio-Token": "wrt_your_token_here"
      }
    }
  }
}
```

**Option B: Query parameter** (Claude Desktop Connectors, authless clients):
```
https://chatdm-334017779319.europe-north1.run.app/mcp?wryterio_token=wrt_your_token_here
```

Generate the token in your Wryterio profile (Profile → API Token → Generate). The token is sent with each request; Wryterio tools use it when `wryterioToken` is not passed explicitly.

**ChatDM server config** (for Wryterio integration): `WRYTERIO_API_URL` is set in deploy config (default: `https://wryterio.com`). To override:
- **Manual deploy**: `WRYTERIO_API_URL=https://your-url.com ./scripts/deploy.sh PROJECT_ID`
- **Cloud Build**: Set substitution `_WRYTERIO_API_URL` in the Cloud Build trigger (Build configuration → Substitution variables).
