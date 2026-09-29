# Firebase notification test

1. Install/run the updated debug build on the phone. Allow notifications through
   the existing Home screen permission flow or Android app notification settings.
2. In Android Studio Logcat, select the phone and filter with `tag:SCA_FCM`.
3. Copy the complete value after `FCM_TOKEN:`. The current token is printed on
   app launch; refreshed tokens are also printed. Release builds do not print tokens.
4. In Firebase project `my-smart-call-assistant`, open Messaging and create a
   Firebase Notifications campaign. Enter a title and body, choose Send test
   message, paste the token, and send the test to that device.
5. Test once with the app open and once after pressing the phone's Home button.
   Do not force-stop the app. Foreground delivery logs `FCM message received`.
   Background notification payloads are displayed by the FCM SDK and normally
   do not invoke the service's message callback.

Notification taps open the existing app launch flow. DND and channel settings can
silence notifications, so check the notification shade too. If token retrieval
fails, look for `FCM token retrieval failed` in Logcat and check network filtering.
FCM delivery and Analytics uploads use different paths: receiving this test does
not by itself verify that Analytics/DebugView uploads work.
