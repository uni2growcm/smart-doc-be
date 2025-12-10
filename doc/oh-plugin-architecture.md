# OH - Plugin Architecture

## General Principles

The OpenHospital plugin architecture must allow the extension of system functionalities **at runtime**, without requiring a rebuild of the frontend or backend application.

### Configuration Flow

1. Plugin configuration is managed centrally by the **backend (server)**
2. The backend handles **plugin bootstrapping** at startup or when new plugins are added
3. The backend exposes to the frontend an **API that describes**:
   - Installed plugins
   - Configuration of each plugin
   - Files to load (JavaScript bundles, configurations, etc.)

## Plugin Structure

### Frontend

A frontend plugin consists of:

- **JSON configuration file**: contains metadata and plugin configuration (simplified structure)
- **JavaScript file**: bundle containing the implementation of the plugin functionalities

### Backend

A backend plugin consists of:

#### Configuration
- JSON configuration file containing the plugin metadata

#### Registration Functions

The plugin must expose functions to register in the OH system:

##### 1. Permissions List
Allows OH to manage user permissions on the functionalities exposed by the plugin. Each defined permission is integrated into the OH authorization system.

##### 2. Internationalized Labels List
Defines translations for all UI labels used by the plugin, supporting OH's multi-language localization.

##### 3. Exposed APIs List

The plugin declares the APIs it exposes. OH handles:

**Route Mapping**: APIs are remapped according to the pattern:
```
http://{oh-host}/api/plugin/{pluginName}/{plugin/api} 
  → http://{plugin-host}/{plugin/api}
```

**Permission-based Filtering**: OH automatically filters requests to the plugin APIs by verifying the permissions associated with the authenticated user.

**Permission-API Mapping**: the plugin must implement a function that defines the relationship between permissions and APIs, allowing OH to correctly apply access policies.


