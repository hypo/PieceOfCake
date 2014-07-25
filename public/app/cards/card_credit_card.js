if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.CreditCardCard = React.createClass({
  displayName: 'CreditCardCard',
  render: function() {
    return (
      React.DOM.div({className: 'content'},
        React.DOM.section({},
          React.DOM.br({}),
          "您可以使用",
          React.DOM.img({src: "/img/visa.png", srcSet: "/img/visa@2x.png 2x", className: "creditcard_logo"}),
          "或",
          React.DOM.img({src: "/img/mastercard.png", srcSet: "/img/mastercard@2x.png 2x", className: "creditcard_logo"}),
          "付款。"
        ),
        React.DOM.form({
          id: "cc_form",
          method: "post",
          action: "/api/initiate-credit-card-transaction"
        },
          React.DOM.section({},
            app.Input({label: "卡號", name: "cardno", defaultValue: this.props.cardno})
          ),
          React.DOM.section({},
            app.Input({label: "有效期限", name: "expiry", placeholder: "MMYY", defaultValue: this.props.expiry})
          ),
          React.DOM.section({},
            app.Input({label: "驗證碼", name: "cvv", placeholder: "卡片後三碼", defaultValue: this.props.cvv})
          )
        )
      )
    );
  }
});