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
      React.DOM.div({className: 'content'},
        React.DOM.section({className: 'logo-container'},
          React.DOM.img({className: 'logo', src: '/img/hypo.png', srcSet: '/img/hypo@2x.png 2x', height: 48})
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field center-placeholder'},
            React.DOM.input({type: 'text', name: 'email', placeholder: 'EMAIL'})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field center-placeholder'},
            React.DOM.input({type: 'text', name: 'password', placeholder: 'PASSWORD'})
          )
        ),
        React.DOM.section({className: 'login'},
          React.DOM.button({className: 'login', onClick: this.handleLogin}, '登入')
        ),
        React.DOM.section({className: 'signup'},
          React.DOM.a({href: '#', onClick: this.handleSignup}, "沒有 hypo 帳號？申請免費帳號。")
        )
      )
    );
  }
});
