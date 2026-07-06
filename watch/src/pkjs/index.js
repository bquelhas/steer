// Steer PebbleKit JS entry point.
//
// Steer has no on-watch settings — everything is configured from the Steer
// companion app on your phone. So the "settings" gear next to Steer in the
// Pebble / Core Devices app simply opens the GitHub releases page, where the
// companion APK can be downloaded.

var APK_DOWNLOAD_URL = 'https://github.com/bquelhas/pebble-steer/releases/latest';

Pebble.addEventListener('showConfiguration', function() {
  Pebble.openURL(APK_DOWNLOAD_URL);
});
