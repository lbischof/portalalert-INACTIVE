var fs = require('fs'),
    ChromeExtension = require('crx'),
    join = require('path').join,
    crx = new ChromeExtension({
      privateKey: fs.readFileSync(join(__dirname, 'extension/key.pem')),
      codebase: 'http://localhost:8000/myFirstExtension.crx',
      rootDirectory: join(__dirname, 'extension/chrome')
    });

crx.load(function(err) {
  if (err) throw err;

  this.pack(function(err, data){
    if (err) throw err;

    console.log(data.toString('base64'));

    this.destroy();
  })
})