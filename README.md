# catalogue-wrapper

A Scala 3 / Play 3.0 **library** that provides a reusable Catalogue/MDTP-styled page layout for HMRC PlatOps frontend services.

## Overview

`catalogue-wrapper-play-30` gives consuming Play frontends:

- A full-page Bootstrap navbar with dynamic menu links
- An integrated quicksearch bar backed by `catalogue-config`
- Packaged CSS and JavaScript assets (Bootstrap 5.3, Bootstrap Icons, search JS)
- A single injected service (`CatalogueWrapperService`) that fetches the menu and returns ready-to-render HTML

Consuming services do **not** need to copy `search.js`, navbar CSS, or menu models.

## Relationship to `catalogue-config`

`catalogue-wrapper` is the **rendering** library.  
`catalogue-config` is the **data** service (full navigation payload: menu structure and search index).

The wrapper calls `catalogue-config` via HTTP for the full navigation payload — two separate calls made in parallel:

```text
GET /menu-bar/menu         → BannerMenu
GET /menu-bar/search-index → Seq[SearchTerm]
```

These are combined internally into `NavigationData(menu, searchIndex)`. The wrapper does not know about upstream Catalogue services, but it does build and query an in-memory optimised search index from the `SearchTerm` data supplied by `catalogue-config`.

#### Partial backend failure behaviour

Both requests start in parallel and are treated as one refresh operation. If either endpoint fails, the full refresh fails and the wrapper falls back to its existing cached `NavigationData`; on a cold cache it falls back to empty navigation data.

This is expected to be acceptable because both endpoints are served by the same `catalogue-config` service, so one endpoint failing while the other succeeds should be unlikely. If that assumption proves wrong, the wrapper could be changed to support partial refreshes — for example using a fresh menu alongside the previously cached search index, or vice versa.

## Internal Auth

The `catalogue-config` menu endpoint is protected by Internal Auth. The wrapper
does not authenticate users itself; it forwards the signed-in user's
`Authorization` credential from the consuming frontend's `HeaderCarrier`.

Consuming frontends must:

1. Use Internal Auth for the page request.
2. Enable the Internal Auth client module:
   ```hocon
   play.modules.enabled += "uk.gov.hmrc.internalauth.client.modules.InternalAuthModule"
   ```
3. Configure the local Internal Auth service:
   ```hocon
   microservice.services.internal-auth {
     protocol = http
     host     = localhost
     port     = 8470
   }
   ```
4. Construct the `HeaderCarrier` from the authenticated request and session
   before calling `CatalogueWrapperService`:
   ```scala
   given HeaderCarrier =
     HeaderCarrierConverter.fromRequestAndSession(request, request.session)
   ```

The menu always includes the direct `Users` link. Additional Users dropdown
entries are controlled by permissions for resource type `catalogue-frontend`:

| Resource type        | Resource location | Action        | Menu entries enabled                            |
| -------------------- | ----------------- | ------------- | ----------------------------------------------- |
| `catalogue-frontend` | `*`               | `CREATE_USER` | Create a User; Create a Service User             |
| `catalogue-frontend` | `*`               | `MANAGE_USER` | Offboard Users                                   |

These permissions are additional to any permissions required by the consuming
frontend itself. For example, a frontend may require its own `READ` permission
to display the page while also granting `catalogue-frontend` / `CREATE_USER` to
display the create-user menu entries.

If neither Catalogue action is present, the direct `Users` link is still
rendered but no Users dropdown is returned. Missing or rejected Internal Auth
credentials cause the menu request to return `401 Unauthorized`.

## Adding the dependency

```sbt
libraryDependencies += "uk.gov.hmrc" %% "catalogue-wrapper-play-30" % "x.y.z"
```

Until published to the HMRC artefact repository, publish locally first:

```bash
sbt publishLocal
```

## Mounting routes

In your service's `conf/app.routes` (or equivalent):

```
-> /catalogue-wrapper cataloguewrapper.Routes
```

This mounts:
- `GET /catalogue-wrapper/quicksearch` — searches the wrapper's locally cached search index (no backend call per query)
- `GET /catalogue-wrapper/assets/*file` — serves wrapper CSS/JS assets

On each page render, `CatalogueWrapperService` attempts to refresh the full navigation payload from `catalogue-config`. If that call fails after at least one prior successful refresh, the wrapper falls back to the cached menu and cached search index. If the backend has never succeeded, the wrapper renders with empty navigation data so the page still loads.

## Using `CatalogueWrapperService`

Inject `CatalogueWrapperService` in your controller:

```scala
class IndexController @Inject()(
    val controllerComponents: MessagesControllerComponents,
    catalogueWrapperService: CatalogueWrapperService,
    pageView: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport:

  def onPageLoad(): Action[AnyContent] =
    Action.async { implicit request =>
      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      catalogueWrapperService.standardCatalogueLayout(
        content      = pageView(myViewModel),
        pageTitle    = Some("My Service"),
        activeItemId = Some("my-service-nav-id"),
        signOutUrl   = Some(controllers.auth.routes.AuthController.signOutNoSurvey().url)
      ).map(Ok(_))
    }
```

Your page view should render **only page content**, not the full layout/menu.

## Configuration

`reference.conf` defaults are provided. Override in your service's `application.conf`:

```hocon
microservice.services.menu-bar {
  protocol = http
  host     = localhost
  port     = 9999
}

# Optional overrides:
catalogue-wrapper {
  quick-search {
    default-limit            = 20
    min-term-length          = 3
    max-terms                = 5
    refresh-throttle-seconds = 30  # seconds between quicksearch retries when index is empty/fallback
  }
  quick-search-path = "/catalogue-wrapper/quicksearch"
  assets-prefix     = "/catalogue-wrapper/assets"
}
```

## Running tests

```bash
sbt clean test
```

## Publishing locally

```bash
sbt publishLocal
```

This publishes to your local Ivy2 cache. Consuming services can then reference the local version.

## Testing from `operational-metrics-frontend`

1. Run `sbt publishLocal` in this repo.
2. In `operational-metrics-frontend/build.sbt`, add:
   ```sbt
   libraryDependencies += "uk.gov.hmrc" %% "catalogue-wrapper-play-30" % "0.1.0-SNAPSHOT"
   ```
3. Mount the routes in `conf/app.routes`.
4. Replace local `BannerMenu`, `MenuLink`, `MenuDropdown`, `SearchTerm` imports with `uk.gov.hmrc.cataloguewrapper.models._`.
5. Remove local `MenuBarConnector` and `QuickSearchController`.
6. Use `CatalogueWrapperService.standardCatalogueLayout(...)` in your controllers.
