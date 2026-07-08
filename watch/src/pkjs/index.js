// Steer PebbleKit JS entry point.
//
// Steer has no on-watch settings — everything is configured from the Steer
// companion app on your phone. The "settings" gear next to Steer in the
// Pebble / Core Devices app therefore opens a tiny SELF-CONTAINED page (a
// data: URI — no hosting, works offline) that says exactly that and offers
// two actions: download the companion APK from GitHub, or close.
// Language follows the phone (Portuguese / English).

var APK_DOWNLOAD_URL = 'https://github.com/bquelhas/pebble-steer/releases/latest';

function configPageHtml() {
  var pt = (navigator.language || '').toLowerCase().indexOf('pt') === 0;
  var t = pt ? {
    body1: 'As definições estão todas na app Steer do telemóvel — o watchapp não tem definições próprias.',
    body2: 'Já tens a app? Abre-a no telemóvel e está feito.',
    download: 'Transferir a app (.apk)',
    close: 'Fechar'
  } : {
    body1: 'All settings live in the Steer app on your phone — the watchapp has no settings of its own.',
    body2: 'Already have the app? Open it on your phone and you are set.',
    download: 'Download the app (.apk)',
    close: 'Close'
  };

  return '<!DOCTYPE html><html><head><meta charset="utf-8">' +
    '<meta name="viewport" content="width=device-width, initial-scale=1">' +
    '<title>Steer</title><style>' +
    'body{margin:0;background:#1e1e1e;font-family:sans-serif;color:#ccc;' +
      'display:flex;justify-content:center;padding:32px 16px}' +
    '.card{max-width:320px;width:100%;background:#2a2a2a;border-radius:16px;' +
      'padding:24px 20px;text-align:center}' +
    'h1{font-size:20px;color:#fff;margin:0 0 10px}' +
    'p{font-size:14px;line-height:1.5;margin:0 0 8px}' +
    'a.btn{display:block;box-sizing:border-box;width:100%;padding:12px;margin-top:14px;' +
      'border-radius:10px;font-size:15px;text-decoration:none}' +
    'a.primary{background:#ff4b49;color:#fff;font-weight:bold}' +
    'a.secondary{background:#3a3a3a;color:#ccc}' +
    '.url{font-size:11px;color:#777;margin-top:16px}' +
    '</style></head><body><div class="card">' +
    '<h1>Steer</h1>' +
    '<p>' + t.body1 + '</p>' +
    '<p>' + t.body2 + '</p>' +
    '<a class="btn primary" href="' + APK_DOWNLOAD_URL + '">' + t.download + '</a>' +
    '<a class="btn secondary" href="pebblejs://close">' + t.close + '</a>' +
    '<div class="url">github.com/bquelhas/pebble-steer</div>' +
    '</div></body></html>';
}

Pebble.addEventListener('showConfiguration', function() {
  Pebble.openURL('data:text/html;charset=utf-8,' + encodeURIComponent(configPageHtml()));
});

// The page closes via pebblejs://close with no payload; nothing to persist.
Pebble.addEventListener('webviewclosed', function() {});
