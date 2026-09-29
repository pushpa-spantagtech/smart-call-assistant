# Firebase tracking

`app/google-services.json` matches application ID `com.smallcallassistant`.
The Google Services and Crashlytics Gradle plugins and Firebase Analytics and
Crashlytics dependencies are configured in the existing Gradle files.

| Event | Trigger | Parameters |
| --- | --- | --- |
| `screen_view` | Requested screen resumes, including return from background | `screen_name`: `login`, `signup`, `home`, `scheduler`, or `support`; `screen_class` |
| `login` | Login API response enters the existing success branch | `method`: `password` |
| `sign_up` | Signup API returns HTTP 2xx and success | `method`: `password` |
| `add_scheduler_click` | Add Scheduler button tapped | `source_screen`: `home` or `scheduler` |
| `schedule_now_click` | Schedule Now submit button tapped | `source_screen`: `scheduler` |
| `logout_confirm` | LOG OUT pressed in the confirmation dialog | `source_screen`: `support` |
| `delete_schedule_confirm` | DELETE pressed in the confirmation dialog | `source_screen`: `home` |
| `turn_off_schedule_confirm` | YES, TURN OFF pressed in the confirmation dialog | `source_screen`: `home` |

Auth events represent successful API outcomes, not initial taps. Other button
events represent clicks/confirmations, not successful persistence. Cancel buttons,
opening dialogs, and changing the schedule mode do not send action events.
No credentials, phone numbers, tokens, or schedule titles are sent by this code.
Automatic screen reporting is disabled to avoid additional Activity screen views.
Firebase still collects its standard automatic events, such as session events.

## Crashlytics behavior

Automatic Crashlytics collection remains enabled. No `recordException` calls or
custom exception handlers are added. Caught application exceptions and ordinary
library logs are therefore not explicitly submitted by this implementation.
Actual uncaught crashes are classified as **fatal**, including crashes originating
in libraries. Crashlytics can also automatically report ANRs. This is not a
fatal-only upload filter; use the console's fatal issue filter for a crash-only view.

## Device verification

1. Install and launch the debug APK on a test device.
2. Run `adb shell setprop debug.firebase.analytics.app com.smallcallassistant`.
3. Open Firebase Analytics DebugView and visit each requested screen.
4. Verify failed login/signup requests do not send `login`/`sign_up`, and successful
   requests each send one matching event.
5. Verify Add Scheduler on both screens and Schedule Now. Cancel each confirmation
   dialog first (no action event), then confirm it (one action event).
6. Disable DebugView mode with `adb shell setprop debug.firebase.analytics.app .none.`.

Validate Crashlytics separately using a deliberate uncaught crash in a temporary
test build, then relaunch to upload the report. No crash-test button is shipped.
