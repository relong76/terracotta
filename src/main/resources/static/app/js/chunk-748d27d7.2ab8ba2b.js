(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-748d27d7"],{"4ff92":function(e,t,n){"use strict";n.r(t);var s=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",[n("h1",[e._v("Create your assignment")]),n("p",[e._v("This will create an unpublished assignment shell in Canvas and will be the way Terracotta will deliver treatments to students.")]),e.experiment&&e.exposures?n("form",{staticClass:"my-5",on:{submit:function(t){return t.preventDefault(),e.saveTitle(t)}}},[n("v-text-field",{attrs:{rules:e.rules,label:"Assignment name",placeholder:"e.g. Lorem ipsum",autofocus:"",outlined:"",required:""},model:{value:e.title,callback:function(t){e.title=t},expression:"title"}}),n("v-btn",{staticClass:"mr-4",attrs:{disabled:!e.title||!e.title.trim(),elevation:"0",color:"primary",type:"submit"}},[e._v(" Next ")])],1):e._e()])},r=[],i=n("5530"),a=(n("498a"),n("2f62")),l={name:"CreateAssignment",props:["experiment"],computed:Object(i["a"])({},Object(a["c"])({exposures:"exposures/exposures"})),data:function(){return{title:"",rules:[function(e){return e&&!!e.trim()||"Assignment Name is required"},function(e){return(e||"").length<=255||"A maximum of 255 characters is allowed"}]}},methods:Object(i["a"])(Object(i["a"])({},Object(a["b"])({createAssignment:"assignment/createAssignment"})),{},{saveTitle:function(){var e=this;this.createAssignment([this.experiment.experimentId,this.exposures[0].exposureId,this.title,1]).then((function(t){201===(null===t||void 0===t?void 0:t.status)?e.$router.push({name:"AssignmentTreatmentSelect",params:{experiment_id:e.experiment.experimentId,assignment_id:t.data.assignmentId}}):alert("error: ",t)})).catch((function(e){console.error("createAssignment | catch",{response:e}),alert("There was an error creating the assignment.")}))}})},u=l,o=n("2877"),c=n("6544"),m=n.n(c),p=n("8336"),d=n("8654"),h=Object(o["a"])(u,s,r,!1,null,null,null);t["default"]=h.exports;m()(h,{VBtn:p["a"],VTextField:d["a"]})}}]);
//# sourceMappingURL=chunk-748d27d7.2ab8ba2b.js.map