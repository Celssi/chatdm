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

## Claude Code Configuration

```bash
claude mcp add --transport http chatdm https://chatdm-334017779319.europe-north1.run.app/mcp
```

## Troubleshooting "handles auth correctly" Error

This error often means the client couldn't connect. Try:

1. **Restart Cursor/Claude** after changing MCP config
2. **Try different transport**: Switch between `sse` and `streamable-http`
3. **Check URL**: Ensure no trailing slash, use `/mcp` for Streamable HTTP
4. **Cold start**: First request may take 10-30s if the service was idle

The server allows unauthenticated access - no API key or auth headers needed.
