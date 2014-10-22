if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.SignupCard = React.createClass({
  displayName: 'SignupCard',
  render: function() {
    return (
      React.DOM.div({className: 'content'},
        React.DOM.section({},
          app.Input({label: _("NAME"), name: "name"})
        ),
        React.DOM.section({},
          app.Input({type:"tel", label: _("TEL"), name: "tel"})
        ),
        React.DOM.section({},
          app.Input({type:"email", label: _("EMAIL"), name: "email", placeholder: _("EMAIL_EXPLANATION")})
        ),
        React.DOM.section({},
          app.Input({label: _("PASSWORD"), name: "password", type: "password"})
        ),
        React.DOM.section({},
          app.Input({label: _("PASSWORD_CONFIRM"), name: "password2", type: "password"})
        )
      )
    );
  }
});
