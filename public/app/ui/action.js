if (window.app === undefined) window.app = {};

function getValueByName(k) {
  return document.querySelector("[name=\"" + k + "\"]").value;
}

function postXHR(path, data, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open("POST", path, true);
  xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xhr.onreadystatechange = function() {
    if (xhr.readyState < 4)
      return;

    callback(xhr);
  }
  var postData = [];
  Object.keys(data).forEach(function(key){
    postData.push(key + "=" + encodeURIComponent(data[key]));
  });
  xhr.send(postData.join("&"));
}

app.handleAction = function(data) {
  data.actions.forEach(function(action) {
    switch (action) {
      case "login":
        var email = getValueByName('email'), pw = getValueByName('password');
        postXHR("/api/login", {email: email, password: pw}, function(xhr){
          if (xhr.status != 200)
            return app.CardStack.flashError("登入錯誤，請稍候再試");

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      case "signup":
        if (getValueByName('password') != getValueByName('password2')) {
          return app.CardStack.flashError("密碼兩次輸入不符，請修正後再試");
        }

        var registerData = {};
        ['name', 'tel', 'email', 'password'].forEach(function(k) {
          registerData[k] = getValueByName(k);
        });

        postXHR("/api/signup", registerData, function(xhr){
          if (xhr.status != 200)
            return app.CardStack.flashError("註冊錯誤，請稍候再試");

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      case "save-data":
        var changes = this.columns.map(function(k) {
          return [k, getValueByName(k)];
        });
        app.CardInfoStore.update(changes);
        break;
      case "push-card":
        app.CardStack.pushCard(this.card)
        break;
      case "push-data":
        app.CardInfoStore.update(this.changes);
        break;
      case "flash-error":
        app.CardStack.flashError(this.error);
        break;
      case "create-order":
        var data = app.CardInfoStore.getData();
        var json = JSON.stringify(data);

        postXHR("/api/create-order", {transaction: json}, function(xhr) {
          if (xhr.status != 200)
            return app.CardStack.flashError("通訊失敗。請稍後再弒");

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      default:
        console.log("[WARN] ActionButton: unrecognized action: " + action);
        break;
    }
  }.bind(data));
}
