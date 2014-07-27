if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.SignupCard = React.createClass({
  displayName: 'SignupCard',
  render: function() {
    return (
      React.DOM.div({className: 'content'},
        React.DOM.section({},
          app.Input({label: "姓名", name: "name"})
        ),
        React.DOM.section({},
          app.Input({label: "電話", name: "tel"})
        ),
        React.DOM.section({},
          app.Input({label: "信箱", name: "email", placeholder: "我們會寄成功訊息到您的信箱"})
        ),
        React.DOM.section({},
          app.Input({label: "密碼", name: "password", type: "password"})
        ),
        React.DOM.section({},
          app.Input({label: "確認密碼", name: "password2", type: "password"})
        )
      )
    );
  }
});
