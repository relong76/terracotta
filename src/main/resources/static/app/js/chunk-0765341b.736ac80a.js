(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-0765341b"],{"20f6":function(t,e,n){},"76e5":function(t,e,n){"use strict";n("e2e3")},8217:function(t,e,n){"use strict";n.r(e);var i=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("h1",[t._v("Name your conditions")]),n("p",[t._v("These will be used to label the different experimental versions of your assignments.")]),t.experiment?n("form",{staticClass:"my-5 mb-15",on:{submit:function(e){return e.preventDefault(),t.saveConditions(e)}}},[n("v-container",{staticClass:"pa-0"},t._l(t.experiment.conditions,(function(e,i){return n("v-row",{key:e.conditionId},[i<2?[n("v-col",{staticClass:"py-0"},[n("v-text-field",{attrs:{name:"condition-"+e.conditionId,rules:t.requiredText,label:"Condition name",placeholder:"e.g. Condition Name",outlined:"",required:""},model:{value:e.name,callback:function(n){t.$set(e,"name",n)},expression:"condition.name"}})],1)]:[n("v-col",{staticClass:"py-0"},[n("v-text-field",{attrs:{name:"condition-"+e.conditionId,rules:t.requiredText,label:"Condition name",placeholder:"e.g. Condition Name",outlined:"",required:""},model:{value:e.name,callback:function(n){t.$set(e,"name",n)},expression:"condition.name"}})],1),n("v-col",{staticClass:"py-0",attrs:{cols:"4",sm:"2"}},[n("v-btn",{staticClass:"delete_condition",attrs:{icon:"",outlined:"",tile:""},on:{click:function(n){return t.deleteCondition(e)}}},[n("v-icon",[t._v("mdi-delete")])],1)],1)]],2)})),1),n("div",[n("v-btn",{staticClass:"add_condition px-0 mb-10",attrs:{color:"blue",text:""},on:{click:function(e){return t.createCondition({name:"",experiment_experiment_id:t.experiment.experimentId})}}},[t._v("Add another condition")])],1),n("v-btn",{staticClass:"mr-4",attrs:{disabled:!t.experiment.conditions.length>0||!t.experiment.conditions.every((function(t){return t.name})),elevation:"0",color:"primary",type:"submit"}},[t._v(" Next ")])],1):t._e()])},o=[],a=n("5530"),r=n("2f62"),s=n("4360"),d={name:"DesignConditions",props:["experiment"],data:function(){return{requiredText:[function(t){return!!t||"Condition name is required"}]}},methods:Object(a["a"])(Object(a["a"])({},Object(r["b"])({createCondition:"condition/createCondition",deleteCondition:"condition/deleteCondition",updateConditions:"condition/updateConditions"})),{},{saveConditions:function(){var t=this,e=this.experiment;this.updateConditions(e.conditions).then((function(e){200===(null===e||void 0===e?void 0:e.status)?t.$router.push({name:"ExperimentDesignType",params:{experiment:t.experiment.experimentId}}):alert(e.error)})).catch((function(t){console.log("updateConditions | catch",{response:t})}))}}),beforeRouteEnter:function(t,e,n){s["a"].state.experiment.experiment.conditions.length<2?s["a"].dispatch("condition/createDefaultConditions",t.params.experiment_id).then((function(){return n()})):n()},beforeRouteUpdate:function(t,e,n){s["a"].state.experiment.experiment.conditions.length<2?s["a"].dispatch("condition/createDefaultConditions",t.params.experiment_id).then((function(){return n()})):n()}},c=d,l=(n("76e5"),n("2877")),u=n("6544"),p=n.n(u),m=n("8336"),f=n("62ad"),v=n("a523"),x=n("132d"),C=n("0fd9"),b=n("8654"),h=Object(l["a"])(c,i,o,!1,null,"18c06379",null);e["default"]=h.exports;p()(h,{VBtn:m["a"],VCol:f["a"],VContainer:v["a"],VIcon:x["a"],VRow:C["a"],VTextField:b["a"]})},a523:function(t,e,n){"use strict";n("4de4"),n("b64b"),n("2ca0"),n("99af"),n("20f6"),n("4b85"),n("498a"),n("a15b");var i=n("2b0e");function o(t){return i["a"].extend({name:"v-".concat(t),functional:!0,props:{id:String,tag:{type:String,default:"div"}},render:function(e,n){var i=n.props,o=n.data,a=n.children;o.staticClass="".concat(t," ").concat(o.staticClass||"").trim();var r=o.attrs;if(r){o.attrs={};var s=Object.keys(r).filter((function(t){if("slot"===t)return!1;var e=r[t];return t.startsWith("data-")?(o.attrs[t]=e,!1):e||"string"===typeof e}));s.length&&(o.staticClass+=" ".concat(s.join(" ")))}return i.id&&(o.domProps=o.domProps||{},o.domProps.id=i.id),e(i.tag,o,a)}})}var a=n("d9f7");e["a"]=o("container").extend({name:"v-container",functional:!0,props:{id:String,tag:{type:String,default:"div"},fluid:{type:Boolean,default:!1}},render:function(t,e){var n,i=e.props,o=e.data,r=e.children,s=o.attrs;return s&&(o.attrs={},n=Object.keys(s).filter((function(t){if("slot"===t)return!1;var e=s[t];return t.startsWith("data-")?(o.attrs[t]=e,!1):e||"string"===typeof e}))),i.id&&(o.domProps=o.domProps||{},o.domProps.id=i.id),t(i.tag,Object(a["a"])(o,{staticClass:"container",class:Array({"container--fluid":i.fluid}).concat(n||[])}),r)}})},e2e3:function(t,e,n){}}]);
//# sourceMappingURL=chunk-0765341b.736ac80a.js.map