# SmartDoc

## Project Overview

### Requirements

Support Adwa Hospital in digitalizing paper documents archived for each patient, so that staff can use OpenHospital (OH) as the sole tool, without having to rely on paper archives.

The intention is to develop a system that is simple from the perspective of:
- technology,
- functionality,
- integration,
- and usability.

The system must integrate into OH to achieve a fully integrated workflow where staff can view medical documents in jpg and pdf formats.

Since the digitalization process must be simplified to the maximum extent, this will be carried out via smartphone, using the camera to digitalize documents and a web application to catalog and insert them into the system.

In order to be reusable in other contexts, it is also required that the software, while integrating with OH, **can also be used in "stand-alone" mode**.

### General Architecture

The system consists of 4 main components:

1. **OH-UI** (modifications): Existing OpenHospital Frontend
2. **OH-API** (modifications): Existing Backend serving as proxy/BFF
3. **Smart-doc BE** (backend): New headless application for document management. It contains also documentation and api spec
4. **Smart-doc UI** (frontend): New React application for document acquisition
