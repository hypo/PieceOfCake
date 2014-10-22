if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.CreditCardCard = React.createClass({
  displayName: 'CreditCardCard',
  render: function() {
    return (
      React.DOM.div({className: 'content card_credit_card'},
        React.DOM.section({className: "description"},
          React.DOM.br({}),
          _("CREDIT_CARD_DESCRIPTION_1"),
          React.DOM.img({src: "/assets/img/visa@2x.png", srcSet: "/assets/img/visa.png 2x, /assets/img/visa@2x.png 2x", className: "creditcard_logo"}),
          _("CREDIT_CARD_DESCRIPTION_2"),
          React.DOM.img({src: "/assets/img/mastercard@2x.png", srcSet: "/assets/img/mastercard.png 1x, /assets/img/mastercard@2x.png 2x", className: "creditcard_logo"}),
          _("CREDIT_CARD_DESCRIPTION_3")
        ),
        React.DOM.form({
          id: "cc_form",
          method: "post",
          action: "/api/initiate-credit-card-transaction"
        },
          React.DOM.section({},
            app.Input({type: "number", pattern: "\\d*", label: _("CREDIT_CARD_NO"), name: "cardno", defaultValue: this.props.cardno})
          ),
          React.DOM.section({},
            app.Input({type: "number", pattern: "\\d*", label: _("CREDIT_CARD_EXPIRY"), name: "expiry", placeholder: "MMYY", defaultValue: this.props.expiry})
          ),
          React.DOM.section({},
            app.Input({type: "number", pattern: "\\d*", label: _("CREDIT_CARD_SECURE_CODE"), name: "cvv", placeholder: _("SECURE_CODE_EXPLANATION"), defaultValue: this.props.cvv})
          )
        )
      )
    );
  }
});
