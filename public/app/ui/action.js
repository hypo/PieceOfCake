if (window.app === undefined) window.app = {};

function getValueByName(k) {
  return document.querySelector("[name=\"" + k + "\"]").value;
}

function getXHR(path, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", path, true);
  xhr.onreadystatechange = function() {
    if (xhr.readyState < 4)
      return;

    callback(xhr);
  }
  xhr.send();
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

        app.CardStack.setShowSpinner(true);
        postXHR("/api/login", {email: email, password: pw}, function(xhr) {
          app.CardStack.setShowSpinner(false);

          if (xhr.status != 200)
            return app.CardStack.flashError(_("LOGIN_ERROR"));

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      case "signup":
        if (getValueByName('password') != getValueByName('password2')) {
          return app.CardStack.flashError(_("PASSWORD_NOT_MATCH"));
        }

        var registerData = {};
        ['name', 'tel', 'email', 'password'].forEach(function(k) {
          registerData[k] = getValueByName(k);
        });

        app.CardStack.setShowSpinner(true);
        postXHR("/api/signup", registerData, function(xhr) {
          app.CardStack.setShowSpinner(false);

          if (xhr.status != 200)
            return app.CardStack.flashError(_("SIGNUP_ERROR"));

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
        app.CardStack.flashError(_(this.error));
        break;
      case "create-order":
        var data = app.CardInfoStore.getData();

        app.CardStack.setShowSpinner(true);
        postXHR("/api/create_order", {
          name: data.name,
          email: data.email,
          tel: data.tel,
          country: data.country,
          area: data.area,
          city: data.city,
          zipcode: data.zipcode,
          addr: data.addr,
          frame_qty: data.frame_qty,
          coupon_code: data.coupon ? data.coupon.code : null
        }, function(xhr) {
          app.CardStack.setShowSpinner(false);
          if (xhr.status != 200)
            return app.CardStack.flashError(_("GENERAL_ERROR"));

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      case "order-cc":
        var data = app.CardInfoStore.getData();

        app.CardStack.setShowSpinner(true);
        postXHR("/api/credit_card", {
          order_id: data.order_id,
          card_no: data.cardno,
          expiry: data.expiry,
          cvv: data.cvv
        }, function(xhr) {
          app.CardStack.setShowSpinner(false);

          if (xhr.status != 200)
            return app.CardStack.flashError(_("GENERAL_ERROR"));

          var data = JSON.parse(xhr.responseText);
          app.handleAction(data);
        });
        break;
      case "done":
        location.href = "pieces://done";
        break;
      default:
        console.log("[WARN] ActionButton: unrecognized action: " + action);
        break;
    }
  }.bind(data));
}
