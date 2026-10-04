# Inventory dashboard delivery

## Confirmed scope

The dashboard describes fridge inventory, not food consumption or nutritional health. Ratios count stored items. Quantity and calorie fields remain compatible with earlier callers but are not used to infer nutrition. Unknown classifications are shown separately and can be corrected manually.

Signed-in inventory uses authenticated account ownership, UUID item identifiers, pending uploads and deletion tombstones. Anonymous inventory is claimed on the first authenticated sync, retained locally if uploading fails, and excluded from other accounts. Local edits made during upload remain pending instead of being overwritten by an older reply. Home and AI share the same repository instance.

Missing-category recommendations open shopping list selection and the existing ingredient picker, filtered to that category. Expiring-item recommendations search recipes using the actual item name. Known quick-add names map to catalog ingredient names; custom names remain editable and can produce no results. Switching accounts clears selections and dashboard history.

## History and storage

- Backend migration: `backend/src/jvmMain/resources/db/migration/V6__Add_inventory_sync_and_dashboard_history.sql` adds account timezone, inventory added date, daily inventory snapshots, expiry snapshots and dashboard events.
- Local migration: `composeApp/src/commonMain/sqldelight/fridger/com/io/database/1.sqm` retains existing inventory as pending anonymous rows. Desktop migration/version updates run in one transaction. Android no longer deletes the database on a schema downgrade.
- A scheduler checks every five minutes and records the actual current date in the stored account timezone. Dashboard reads refresh that day's snapshot. Missing historical days stay missing; API metadata flags a partial range.
- Snapshot replacement locks the account row in the database so requests and schedulers serialize even across backend processes. One failed account capture is logged and does not stop other accounts.
- Client analytics send authenticated section toggle, collapsed impression, state sync, dashboard view and recommendation action events to the persisted backend event endpoint. These provide Beta inputs; Looker configuration and real interaction/expiry-rate measurements require deployment and users.

## Validation

Run from the task worktree with the existing Android SDK:

```sh
ANDROID_HOME=/Users/wixtar-kyle/Library/Android/sdk ./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinDesktop :composeApp:desktopTest :backend:test :shared:desktopTest --no-daemon --max-workers=2
```

Regression coverage includes anonymous inventory merging, failed upload/delete retry, account isolation, category edits/deletes during upload, legacy SQLite migration, dashboard account switching, inventory ownership protection, stored timezone day boundaries and real snapshot/item-count history.

Build blockers discovered during recovery were the absent common fridge image, absent Desktop connectivity actual, outdated recipe test fake signatures and JSON-list sanitization incorrectly stripping quoted/bracketed ingredient labels. The existing Android fridge vector is reused, existing platform/dependency features provide connectivity, and the sanitizer recognizes actual JSON array syntax.

Remaining checks: physical Android/iOS UI, iOS build, production PostgreSQL/Flyway migration and live Google sign-in/cross-device synchronization have not been exercised here. Backend repository tests use H2; desktop tests use SQLite. Existing Gradle/JVM/Google sign-in/Exposed deprecation and SDK-version warnings remain.

## Operational limits

The scheduler scans accounts every five minutes. Desktop connectivity detects interface availability rather than proving internet reachability; failed HTTP work remains pending and refresh retries it. Analytics are best effort and do not queue offline events. The new backend API/migration must be deployed before cloud history is available; local inventory insights continue to work offline.
