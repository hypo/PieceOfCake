if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.LoginCard = React.createClass({
  displayName: 'LoginCard',
  handleLogin: function() {
    app.handleAction({
      actions: ['login']
    });
  },
  handleSignup: function() {
    app.handleAction({
      actions: ['push-card'],
      card: 'card_signup'
    });
  },
  render: function() {
    return (
      React.DOM.div({className: 'content card_login'},
        React.DOM.section({className: 'logo-container'},
          React.DOM.img({className: 'logo', src: '/assets/img/hypo@2x.png', srcSet: '/assets/img/hypo.png 1x, /assets/img/hypo@2x.png 2x', height: 44})
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field center-placeholder'},
            React.DOM.input({type: 'email', name: 'email', placeholder: _('EMAIL_PLACEHOLDER')})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field center-placeholder'},
            React.DOM.input({type: 'password', name: 'password', placeholder: _('PASSWORD_PLACEHOLDER')})
          )
        ),
        React.DOM.section({className: 'login'},
          React.DOM.button({className: 'login', onClick: this.handleLogin}, _('LOGIN'))
        ),
        React.DOM.section({className: 'signup'},
          React.DOM.button({className: 'signup', onClick: this.handleSignup}, _('SIGNUP'))
        ),
        React.DOM.section({className: 'signup'},
          React.DOM.a({href: '#', onClick: this.handleSignup}, _("GO_SIGNUP_ACCOUNT"))
        )
      )
    );
  }
});
