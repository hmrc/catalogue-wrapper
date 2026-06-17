# catalogue-wrapper

A Scala 3 / Play 3.0 **library** that provides a reusable Catalogue/MDTP-styled page layout for HMRC PlatOps frontend services.

## Overview

`catalogue-wrapper-play-30` gives consuming Play frontends:

- A full-page Bootstrap navbar with dynamic menu links
- An integrated quicksearch bar backed by `menu-bar-backend`
- Packaged CSS and JavaScript assets (Bootstrap 5.3, Bootstrap Icons, search JS)
- A single injected service (`CatalogueWrapperService`) that fetches the menu and returns ready-to-render HTML

Consuming services do **not** need to copy `search.js`, navbar CSS, or menu models.

## Relationship to `menu-bar-backend`

`catalogue-wrapper` is the **rendering** library.  
`menu-bar-backend` is the **data** service (menu structure and search index).

The wrapper calls `menu-bar-backend` via HTTP. It does not rebuild the search index or know about upstream Catalogue services.

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
- `GET /catalogue-wrapper/quicksearch` — proxies search to `menu-bar-backend`
- `GET /catalogue-wrapper/assets/*file` — serves wrapper CSS/JS assets

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
