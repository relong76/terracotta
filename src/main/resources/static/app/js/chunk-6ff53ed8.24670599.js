(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-6ff53ed8"],{4193:function(t,e,a){"use strict";a.r(e);var s=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("v-card",{staticClass:"mb-8 pt-5 px-5 mx-auto red lighten-5 rounded-lg",attrs:{outlined:""}},[a("p",[a("strong",[t._v("Are you sure you want to include all students in your experiment automatically?")])]),a("p",[t._v("One of the basic principles of ethical research is showing respect for research participants. One way of showing this respect is by providing people an opportunity to make decisions for themselves about whether they want to participate in a study.")]),a("p",[t._v("Terracotta is designed to make this process easy. If you want, we can create as short assignment where your students will provide consent to be included in this experiment.")])]),a("v-btn",{staticClass:"mb-4",attrs:{elevation:"0",color:"primary"},on:{click:t.setType}},[t._v("Yes, I want to proceed")]),a("br"),a("router-link",{staticClass:"plain-link",attrs:{to:{name:"ParticipationDistribution"}}},[t._v("No, I want to create a consent assignment instead")])],1)},i=[],n=a("5530"),r=a("2f62"),o={name:"ParticipationTypeAutoConfirm",methods:Object(n["a"])(Object(n["a"])({},Object(r["b"])({updateExperiment:"experiment/updateExperiment"})),{},{setType:function(){}})},c=o,l=(a("e36a"),a("2877")),d=a("6544"),u=a.n(d),p=a("8336"),h=a("b0af"),b=Object(l["a"])(c,s,i,!1,null,"4ce9f3bc",null);e["default"]=b.exports;u()(b,{VBtn:p["a"],VCard:h["a"]})},"615b":function(t,e,a){},8982:function(t,e,a){},b0af:function(t,e,a){"use strict";var s=a("5530"),i=(a("a9e3"),a("0481"),a("615b"),a("10d2")),n=a("297c"),r=a("1c87"),o=a("58df");e["a"]=Object(o["a"])(n["a"],r["a"],i["a"]).extend({name:"v-card",props:{flat:Boolean,hover:Boolean,img:String,link:Boolean,loaderHeight:{type:[Number,String],default:4},raised:Boolean},computed:{classes:function(){return Object(s["a"])(Object(s["a"])({"v-card":!0},r["a"].options.computed.classes.call(this)),{},{"v-card--flat":this.flat,"v-card--hover":this.hover,"v-card--link":this.isClickable,"v-card--loading":this.loading,"v-card--disabled":this.disabled,"v-card--raised":this.raised},i["a"].options.computed.classes.call(this))},styles:function(){var t=Object(s["a"])({},i["a"].options.computed.styles.call(this));return this.img&&(t.background='url("'.concat(this.img,'") center center / cover no-repeat')),t}},methods:{genProgress:function(){var t=n["a"].options.methods.genProgress.call(this);return t?this.$createElement("div",{staticClass:"v-card__progress",key:"progress"},[t]):null}},render:function(t){var e=this.generateRouteLink(),a=e.tag,s=e.data;return s.style=this.styles,this.isClickable&&(s.attrs=s.attrs||{},s.attrs.tabindex=0),t(a,this.setBackgroundColor(this.color,s),[this.genProgress(),this.$slots.default])}})},e36a:function(t,e,a){"use strict";a("8982")}}]);
//# sourceMappingURL=chunk-6ff53ed8.24670599.js.map