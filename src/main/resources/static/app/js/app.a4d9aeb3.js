(function(e){function t(t){for(var c,a,i=t[0],s=t[1],u=t[2],p=0,d=[];p<i.length;p++)a=i[p],Object.prototype.hasOwnProperty.call(o,a)&&o[a]&&d.push(o[a][0]),o[a]=0;for(c in s)Object.prototype.hasOwnProperty.call(s,c)&&(e[c]=s[c]);l&&l(t);while(d.length)d.shift()();return r.push.apply(r,u||[]),n()}function n(){for(var e,t=0;t<r.length;t++){for(var n=r[t],c=!0,a=1;a<n.length;a++){var i=n[a];0!==o[i]&&(c=!1)}c&&(r.splice(t--,1),e=s(s.s=n[0]))}return e}var c={},a={app:0},o={app:0},r=[];function i(e){return s.p+"js/"+({}[e]||e)+"."+{"chunk-16a4f436":"e2ff2fcf","chunk-1af076e8":"86cc7127","chunk-1c52ac5c":"c942cd7a","chunk-2d0ba0bb":"a87a01ad","chunk-2d0d7488":"6d94c326","chunk-2d0e5d09":"a9064f30","chunk-2d207d1a":"cc0e98c0","chunk-2d20f689":"c2716247","chunk-2d216fd2":"77f37ea9","chunk-2d22c525":"4a13cdf4","chunk-4257b527":"e081345b","chunk-530c7014":"948cacf9","chunk-602f794d":"b57388c8","chunk-6dbac39e":"3b96fd9e","chunk-6ff53ed8":"24670599","chunk-748d27d7":"be882bda","chunk-7c0220d4":"1241bc39","chunk-7c2b03ee":"9c249f9c","chunk-7c39e644":"46280279","chunk-7eb255ce":"1d091ae3","chunk-a40b083a":"3fbdba9e","chunk-e2add046":"fceea12f","chunk-ef639f40":"80c4cb7b","chunk-f7d7c106":"922a1764"}[e]+".js"}function s(t){if(c[t])return c[t].exports;var n=c[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,s),n.l=!0,n.exports}s.e=function(e){var t=[],n={"chunk-16a4f436":1,"chunk-1af076e8":1,"chunk-1c52ac5c":1,"chunk-4257b527":1,"chunk-530c7014":1,"chunk-602f794d":1,"chunk-6dbac39e":1,"chunk-6ff53ed8":1,"chunk-7c0220d4":1,"chunk-7c2b03ee":1,"chunk-7c39e644":1,"chunk-7eb255ce":1,"chunk-a40b083a":1,"chunk-e2add046":1,"chunk-ef639f40":1,"chunk-f7d7c106":1};a[e]?t.push(a[e]):0!==a[e]&&n[e]&&t.push(a[e]=new Promise((function(t,n){for(var c="css/"+({}[e]||e)+"."+{"chunk-16a4f436":"2106a01e","chunk-1af076e8":"3bed4002","chunk-1c52ac5c":"408b9ed5","chunk-2d0ba0bb":"31d6cfe0","chunk-2d0d7488":"31d6cfe0","chunk-2d0e5d09":"31d6cfe0","chunk-2d207d1a":"31d6cfe0","chunk-2d20f689":"31d6cfe0","chunk-2d216fd2":"31d6cfe0","chunk-2d22c525":"31d6cfe0","chunk-4257b527":"c0ee0b44","chunk-530c7014":"c542b0fa","chunk-602f794d":"1fc8bdc4","chunk-6dbac39e":"1707b3b0","chunk-6ff53ed8":"32982a55","chunk-748d27d7":"31d6cfe0","chunk-7c0220d4":"509272a8","chunk-7c2b03ee":"509272a8","chunk-7c39e644":"509272a8","chunk-7eb255ce":"3bed4002","chunk-a40b083a":"408b9ed5","chunk-e2add046":"5de263fd","chunk-ef639f40":"3bed4002","chunk-f7d7c106":"76c833bc"}[e]+".css",o=s.p+c,r=document.getElementsByTagName("link"),i=0;i<r.length;i++){var u=r[i],p=u.getAttribute("data-href")||u.getAttribute("href");if("stylesheet"===u.rel&&(p===c||p===o))return t()}var d=document.getElementsByTagName("style");for(i=0;i<d.length;i++){u=d[i],p=u.getAttribute("data-href");if(p===c||p===o)return t()}var l=document.createElement("link");l.rel="stylesheet",l.type="text/css",l.onload=t,l.onerror=function(t){var c=t&&t.target&&t.target.src||o,r=new Error("Loading CSS chunk "+e+" failed.\n("+c+")");r.code="CSS_CHUNK_LOAD_FAILED",r.request=c,delete a[e],l.parentNode.removeChild(l),n(r)},l.href=o;var f=document.getElementsByTagName("head")[0];f.appendChild(l)})).then((function(){a[e]=0})));var c=o[e];if(0!==c)if(c)t.push(c[2]);else{var r=new Promise((function(t,n){c=o[e]=[t,n]}));t.push(c[2]=r);var u,p=document.createElement("script");p.charset="utf-8",p.timeout=120,s.nc&&p.setAttribute("nonce",s.nc),p.src=i(e);var d=new Error;u=function(t){p.onerror=p.onload=null,clearTimeout(l);var n=o[e];if(0!==n){if(n){var c=t&&("load"===t.type?"missing":t.type),a=t&&t.target&&t.target.src;d.message="Loading chunk "+e+" failed.\n("+c+": "+a+")",d.name="ChunkLoadError",d.type=c,d.request=a,n[1](d)}o[e]=void 0}};var l=setTimeout((function(){u({type:"timeout",target:p})}),12e4);p.onerror=p.onload=u,document.head.appendChild(p)}return Promise.all(t)},s.m=e,s.c=c,s.d=function(e,t,n){s.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},s.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},s.t=function(e,t){if(1&t&&(e=s(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(s.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var c in e)s.d(n,c,function(t){return e[t]}.bind(null,c));return n},s.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return s.d(t,"a",t),t},s.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},s.p="/app/",s.oe=function(e){throw console.error(e),e};var u=window["webpackJsonp"]=window["webpackJsonp"]||[],p=u.push.bind(u);u.push=t,u=u.slice();for(var d=0;d<u.length;d++)t(u[d]);var l=p;r.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},"1f92":function(e,t,n){e.exports=n.p+"img/terracotta_logo.1ce6c1df.svg"},"21bb":function(e,t,n){"use strict";n("2dad")},"2dad":function(e,t,n){},4360:function(e,t,n){"use strict";var c=n("2b0e"),a=n("2f62"),o=n("0e44"),r=n("5530");n("d3b7"),n("99af");function i(){var e;return null!==(e=ye.state.api)&&void 0!==e&&e.api_token?{Authorization:"Bearer "+ye.state.api.api_token,"Content-Type":"application/json"}:{}}var s={getApiToken:u,refreshToken:p,reportStep:d};function u(e){var t={method:"POST",headers:{Authorization:"Bearer ".concat(e),"Content-Type":"application/json"}};return fetch("".concat(ye.getters["api/aud"],"/api/oauth/trade"),t).then((function(e){if(e.ok)return e.text()}))}function p(){var e={method:"POST",headers:Object(r["a"])({},i())};return fetch("".concat(ye.getters["api/aud"],"/api/oauth/refresh"),e).then((function(e){if(e.ok)return console.log(e.text()),e.text()}))}function d(e,t){var n={method:"POST",headers:Object(r["a"])({},i()),body:JSON.stringify({step:t})};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/step"),n).then((function(e){if(e.ok)return console.log(e.text()),e.text()}))}var l={getAll:f,getById:m,create:h,update:b,delete:g};function f(){var e={method:"GET",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments"),e).then(v)}function h(){var e={method:"POST",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"}),body:JSON.stringify({})};return fetch("".concat(ye.getters["api/aud"],"/api/experiments"),e).then(v)}function m(e){var t={method:"GET",headers:Object(r["a"])({},i())};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"?conditions=true"),t).then(v)}function b(e){var t={method:"PUT",headers:Object(r["a"])({},i()),body:JSON.stringify(e)};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e.experimentId),t).then(v)}function g(e){var t={method:"DELETE",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e),t).then(v)}function v(e){return e.text().then((function(t){var n=t&&JSON.parse(t);return e&&e.ok?204===e.status?(console.log("handleResponse | 204",{text:t,data:n,response:e}),[]):n||e:(401===e.status||402===e.status||500===e.status?console.log("handleResponse | 401/402/500",{response:e}):404===e.status&&console.log("handleResponse | 404",{response:e}),e)})).catch((function(e){console.log("handleResponse | catch",{text:e})}))}var j={create:k,update:x,delete:y};function k(e){var t={method:"POST",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"}),body:JSON.stringify(e)};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e.experiment_experiment_id,"/conditions"),t).then(_)}function x(e){var t={method:"PUT",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"}),body:JSON.stringify(e)};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e.experimentId,"/conditions/").concat(e.conditionId),t).then(_)}function y(e){var t={method:"DELETE",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"})};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e.experimentId,"/conditions/").concat(e.conditionId),t).then(_)}function _(e){return e.text().then((function(t){var n=t&&JSON.parse(t);return e&&e.ok?(console.log("handleResponse | then",{text:t,data:n,response:e}),n||e):(401===e.status||402===e.status||500===e.status?console.log("handleResponse | 401/402/500",{response:e}):404===e.status&&console.log("handleResponse | 404",{response:e}),e)})).catch((function(e){console.log("handleResponse | catch",{text:e})}))}var S=n("bc3a"),E=n.n(S),T={create:C,update:O,delete:w};function C(e,t){var n={headers:Object(r["a"])({"Content-Type":"multipart/form-data"},i())},c=new FormData;return c.append("consent",t.file),E.a.post("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/consent?title=").concat(t.title),c,n).then(I)}function O(e){var t={method:"PUT",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"})};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/consent"),t).then(I)}function w(e){var t={method:"DELETE",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"})};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/consent"),t).then(I)}function I(e){return e.text().then((function(t){var n=t&&JSON.parse(t);return e&&e.ok?(console.log("handleResponse | then",{text:t,data:n,response:e}),n||e):(401===e.status||402===e.status||500===e.status?console.log("handleResponse | 401/402/500",{response:e}):404===e.status&&console.log("handleResponse | 404",{response:e}),e)})).catch((function(e){console.log("handleResponse | catch",{text:e})}))}var P={getAll:A,getById:D};function A(e){var t={method:"GET",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/exposures"),t).then(z)}function D(e,t){var n={method:"GET",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/exposures/").concat(t),n).then(z)}function z(e){return e.text().then((function(t){var n=t&&JSON.parse(t);return e&&e.ok?(console.log("handleResponse | then",{text:t,data:n,response:e}),n||e):(401===e.status||402===e.status||500===e.status?console.log("handleResponse | 401/402/500",{response:e}):404===e.status&&console.log("handleResponse | 404",{response:e}),e)})).catch((function(e){console.log("handleResponse | catch",{text:e})}))}var R={getAll:L,getById:N,updateParticipants:V};function L(e){var t={method:"GET",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/participants"),t).then(B)}function N(e,t){var n={method:"GET",headers:i()};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/participants/").concat(t),n).then(B)}function V(e,t){var n={method:"PUT",headers:Object(r["a"])(Object(r["a"])({},i()),{},{"Content-Type":"application/json"}),body:JSON.stringify(t)};return fetch("".concat(ye.getters["api/aud"],"/api/experiments/").concat(e,"/participants"),n).then(B)}function B(e){return e.text().then((function(t){var n=t&&JSON.parse(t);return e&&e.ok?(console.log("handleResponse | then",{text:t,data:n,response:e}),n||e):(401===e.status||402===e.status||500===e.status?console.log("handleResponse | 401/402/500",{response:e}):404===e.status&&console.log("handleResponse | 404",{response:e}),e)})).catch((function(e){console.log("handleResponse | catch",{text:e})}))}var J=n("1232"),G={lti_token:"",api_token:"",aud:""},M={setLtiToken:function(e,t){var n=e.commit,c=e.dispatch,a=Object(J["a"])(t);console.log(a),n("setLtiToken",t),n("setAud",a.aud),c("setApiToken",t)},setApiToken:function(e,t){var n=e.commit;return s.getApiToken(t).then((function(e){"string"===typeof e&&n("setApiToken",e)})).catch((function(e){console.log("setApiToken | catch",{response:e})}))},refreshToken:function(e){var t=e.commit;return s.refreshToken().then((function(e){"string"===typeof e&&t("setApiToken",e)})).catch((function(e){console.log("refreshToken | catch",{response:e})}))},reportStep:function(e,t){var n=e.state,c=t.experimentId,a=t.step;return s.reportStep(c,a).then((function(e){console.log("reportStep | then",{state:n,data:e})})).catch((function(e){console.log("reportStep | catch",{response:e})}))}},F={setLtiToken:function(e,t){e.lti_token=t},setApiToken:function(e,t){e.api_token=t},setAud:function(e,t){e.aud=t}},U={lti_token:function(e){return e.lti_token},aud:function(e){return e.aud},hasTokens:function(e){return e.lti_token.length>0&&e.api_token.length>0}},$={namespaced:!0,state:G,actions:M,mutations:F,getters:U},q={type:null,message:null},H={success:function(e,t){var n=e.commit;n("success",t)},error:function(e,t){var n=e.commit;n("error",t)},clear:function(e,t){var n=e.commit;n("success",t)}},K={success:function(e,t){e.type="alert-success",e.message=t},error:function(e,t){e.type="alert-danger",e.message=t},clear:function(e){e.type=null,e.message=null}},W={namespaced:!0,state:q,actions:H,mutations:K},Q=(n("c740"),n("4de4"),{experiment:null,experiments:null}),X={resetExperiment:function(e){var t=e.commit;t("setExperiment",{})},createExperiment:function(){return l.create()},fetchExperimentById:function(e,t){var n=e.commit;return l.getById(t).then((function(e){n("setExperiment",e)})).catch((function(e){console.log("fetchExperimentById | catch",{response:e})}))},fetchExperiments:function(e){var t=e.commit;return l.getAll().then((function(e){t("setExperiments",e)})).catch((function(e){console.log("fetchExperimentById | catch",{response:e})}))},updateExperiment:function(e,t){var n=e.commit;return l.update(t).then(n("setExperiment",t)).catch((function(e){console.log("updateExperiment | catch",{response:e})}))},deleteExperiment:function(e,t){var n=e.commit;return l.delete(t).then((function(e){200===(null===e||void 0===e?void 0:e.status)&&n("deleteExperiment",t)})).catch((function(e){console.log("deleteExperiment | catch",{response:e})}))}},Y={setExperiment:function(e,t){e.experiment=t},setExperiments:function(e,t){e.experiments=t},setConditions:function(e,t){e.experiment.conditions=t},setCondition:function(e,t){var n=e.experiment.conditions.findIndex((function(e){return e.conditionId===t.conditionId}));n>=0?e.experiment.conditions[n]=t:e.experiment.conditions.push(t)},deleteExperiment:function(e,t){e.experiments=e.experiments.filter((function(e){return e.experimentId!==t}))},deleteCondition:function(e,t){e.experiment.conditions=e.experiment.conditions.filter((function(e){return e.conditionId!==t.conditionId}))}},Z={conditions:function(e){return e.experiment.conditions},experiments:function(e){return e.experiments}},ee={namespaced:!0,state:Q,actions:X,mutations:Y,getters:Z},te=(n("159b"),{}),ne={resetCondition:function(e){var t=e.commit;t("setCondition",{})},createDefaultConditions:function(e,t){var n=e.dispatch,c=[{name:"",experiment_experiment_id:t},{name:"",experiment_experiment_id:t}];n("createConditions",c)},createConditions:function(e,t){var n=e.dispatch;t.length>0&&t.forEach((function(e){n("createCondition",e)}))},createCondition:function(e,t){var n=e.commit;return j.create(t).then((function(e){n("experiment/setCondition",e,{root:!0})})).catch((function(e){console.log("createCondition | catch",{response:e})}))},updateConditions:function(e,t){var n=e.dispatch;if(t.length>0)return t.forEach((function(e){null!==e&&void 0!==e&&e.conditionId&&n("updateCondition",e)})),{status:200}},updateCondition:function(e,t){var n=e.commit;return j.update(t).then((function(e){null!==e&&void 0!==e&&e.conditionId&&n("experiment/updateCondition",e,{root:!0})})).catch((function(e){console.log("setCondition | catch",{response:e})}))},setDefaultCondition:function(e,t){var n=e.dispatch;return!!(t&&t.conditions&&t.defaultConditionId)&&(t.conditions.forEach((function(e){return e.defaultCondition=e.conditionId===t.defaultConditionId?1:0,n("updateCondition",e),e})),{status:200})},deleteCondition:function(e,t){var n=e.commit;return j.delete(t).then((function(){n("experiment/deleteCondition",t,{root:!0})})).catch((function(e){console.log("setCondition | catch",{response:e})}))}},ce={},ae={},oe={namespaced:!0,state:te,actions:ne,mutations:ce,getters:ae},re={file:null,title:""},ie={resetConsent:function(e){var t=e.commit;t("setConsent",{})},createConsent:function(e,t){var n=e.state;return T.create(t,n).catch((function(e){console.log("setCondition | catch",{response:e})}))},setConsentFile:function(e,t){var n=e.commit;n("setConsentFile",t)}},se={setConsent:function(e,t){t},setConsentTitle:function(e,t){e.title=t},setConsentFile:function(e,t){e.file=t}},ue={consent:function(e){return e}},pe={namespaced:!0,state:re,actions:ie,mutations:se,getters:ue},de={exposures:null},le={fetchExposures:function(e,t){var n=e.commit;return P.getAll(t).then((function(e){n("setExposuresService",e)})).catch((function(e){console.log("fetchExposures | catch",{response:e})}))}},fe={setExposuresService:function(e,t){e.exposures=t}},he={exposures:function(e){return e.exposures}},me={namespaced:!0,state:de,actions:le,mutations:fe,getters:he},be=(n("d81d"),{participants:null,groups:null}),ge={fetchParticipants:function(e,t){var n=e.commit;return R.getAll(t).then((function(e){n("setParticipants",e)})).catch((function(e){console.log("fetchParticipants | catch",{response:e})}))},setParticipantsGroup:function(e,t){var n=e.commit;n("setParticipantsGroup",t)},updateParticipants:function(e,t){var n=e.state,c=[];return n.participants.map((function(e){var t={participantId:e.participantId,consent:e.consent,dropped:e.dropped,groupId:e.groupId};c.push(t)})),R.updateParticipants(t,c).catch((function(e){return console.log("updateParticipants | catch",{response:e})}))},fetchGroups:function(e,t){var n=e.commit;return R.getGroups(t).then((function(e){n("setGroups",e)})).catch((function(e){console.log("fetchParticipants | catch",{response:e})}))}},ve={setParticipants:function(e,t){e.participants=t},setParticipantsGroup:function(e,t){e.participants=t}},je={participants:function(e){return e.participants}},ke={namespaced:!0,state:be,actions:ge,mutations:ve,getters:je};c["a"].use(a["a"]);var xe=new a["a"].Store({plugins:[Object(o["a"])({key:"terracotta"})],modules:{api:$,alert:W,experiment:ee,condition:oe,consent:pe,exposures:me,participants:ke}}),ye=t["a"]=xe},4678:function(e,t,n){var c={"./af":"2bfb","./af.js":"2bfb","./ar":"8e73","./ar-dz":"a356","./ar-dz.js":"a356","./ar-kw":"423e","./ar-kw.js":"423e","./ar-ly":"1cfd","./ar-ly.js":"1cfd","./ar-ma":"0a84","./ar-ma.js":"0a84","./ar-sa":"8230","./ar-sa.js":"8230","./ar-tn":"6d83","./ar-tn.js":"6d83","./ar.js":"8e73","./az":"485c","./az.js":"485c","./be":"1fc1","./be.js":"1fc1","./bg":"84aa","./bg.js":"84aa","./bm":"a7fa","./bm.js":"a7fa","./bn":"9043","./bn-bd":"9686","./bn-bd.js":"9686","./bn.js":"9043","./bo":"d26a","./bo.js":"d26a","./br":"6887","./br.js":"6887","./bs":"2554","./bs.js":"2554","./ca":"d716","./ca.js":"d716","./cs":"3c0d","./cs.js":"3c0d","./cv":"03ec","./cv.js":"03ec","./cy":"9797","./cy.js":"9797","./da":"0f14","./da.js":"0f14","./de":"b469","./de-at":"b3eb","./de-at.js":"b3eb","./de-ch":"bb71","./de-ch.js":"bb71","./de.js":"b469","./dv":"598a","./dv.js":"598a","./el":"8d47","./el.js":"8d47","./en-au":"0e6b","./en-au.js":"0e6b","./en-ca":"3886","./en-ca.js":"3886","./en-gb":"39a6","./en-gb.js":"39a6","./en-ie":"e1d3","./en-ie.js":"e1d3","./en-il":"7333","./en-il.js":"7333","./en-in":"ec2e","./en-in.js":"ec2e","./en-nz":"6f50","./en-nz.js":"6f50","./en-sg":"b7e9","./en-sg.js":"b7e9","./eo":"65db","./eo.js":"65db","./es":"898b","./es-do":"0a3c","./es-do.js":"0a3c","./es-mx":"b5b7","./es-mx.js":"b5b7","./es-us":"55c9","./es-us.js":"55c9","./es.js":"898b","./et":"ec18","./et.js":"ec18","./eu":"0ff2","./eu.js":"0ff2","./fa":"8df4","./fa.js":"8df4","./fi":"81e9","./fi.js":"81e9","./fil":"d69a","./fil.js":"d69a","./fo":"0721","./fo.js":"0721","./fr":"9f26","./fr-ca":"d9f8","./fr-ca.js":"d9f8","./fr-ch":"0e49","./fr-ch.js":"0e49","./fr.js":"9f26","./fy":"7118","./fy.js":"7118","./ga":"5120","./ga.js":"5120","./gd":"f6b4","./gd.js":"f6b4","./gl":"8840","./gl.js":"8840","./gom-deva":"aaf2","./gom-deva.js":"aaf2","./gom-latn":"0caa","./gom-latn.js":"0caa","./gu":"e0c5","./gu.js":"e0c5","./he":"c7aa","./he.js":"c7aa","./hi":"dc4d","./hi.js":"dc4d","./hr":"4ba9","./hr.js":"4ba9","./hu":"5b14","./hu.js":"5b14","./hy-am":"d6b6","./hy-am.js":"d6b6","./id":"5038","./id.js":"5038","./is":"0558","./is.js":"0558","./it":"6e98","./it-ch":"6f12","./it-ch.js":"6f12","./it.js":"6e98","./ja":"079e","./ja.js":"079e","./jv":"b540","./jv.js":"b540","./ka":"201b","./ka.js":"201b","./kk":"6d79","./kk.js":"6d79","./km":"e81d","./km.js":"e81d","./kn":"3e92","./kn.js":"3e92","./ko":"22f8","./ko.js":"22f8","./ku":"2421","./ku.js":"2421","./ky":"9609","./ky.js":"9609","./lb":"440c","./lb.js":"440c","./lo":"b29d","./lo.js":"b29d","./lt":"26f9","./lt.js":"26f9","./lv":"b97c","./lv.js":"b97c","./me":"293c","./me.js":"293c","./mi":"688b","./mi.js":"688b","./mk":"6909","./mk.js":"6909","./ml":"02fb","./ml.js":"02fb","./mn":"958b","./mn.js":"958b","./mr":"39bd","./mr.js":"39bd","./ms":"ebe4","./ms-my":"6403","./ms-my.js":"6403","./ms.js":"ebe4","./mt":"1b45","./mt.js":"1b45","./my":"8689","./my.js":"8689","./nb":"6ce3","./nb.js":"6ce3","./ne":"3a39","./ne.js":"3a39","./nl":"facd","./nl-be":"db29","./nl-be.js":"db29","./nl.js":"facd","./nn":"b84c","./nn.js":"b84c","./oc-lnc":"167b","./oc-lnc.js":"167b","./pa-in":"f3ff","./pa-in.js":"f3ff","./pl":"8d57","./pl.js":"8d57","./pt":"f260","./pt-br":"d2d4","./pt-br.js":"d2d4","./pt.js":"f260","./ro":"972c","./ro.js":"972c","./ru":"957c","./ru.js":"957c","./sd":"6784","./sd.js":"6784","./se":"ffff","./se.js":"ffff","./si":"eda5","./si.js":"eda5","./sk":"7be6","./sk.js":"7be6","./sl":"8155","./sl.js":"8155","./sq":"c8f3","./sq.js":"c8f3","./sr":"cf1e","./sr-cyrl":"13e9","./sr-cyrl.js":"13e9","./sr.js":"cf1e","./ss":"52bd","./ss.js":"52bd","./sv":"5fbd","./sv.js":"5fbd","./sw":"74dc","./sw.js":"74dc","./ta":"3de5","./ta.js":"3de5","./te":"5cbb","./te.js":"5cbb","./tet":"576c","./tet.js":"576c","./tg":"3b1b","./tg.js":"3b1b","./th":"10e8","./th.js":"10e8","./tk":"5aff","./tk.js":"5aff","./tl-ph":"0f38","./tl-ph.js":"0f38","./tlh":"cf75","./tlh.js":"cf75","./tr":"0e81","./tr.js":"0e81","./tzl":"cf51","./tzl.js":"cf51","./tzm":"c109","./tzm-latn":"b53d","./tzm-latn.js":"b53d","./tzm.js":"c109","./ug-cn":"6117","./ug-cn.js":"6117","./uk":"ada2","./uk.js":"ada2","./ur":"5294","./ur.js":"5294","./uz":"2e8c","./uz-latn":"010e","./uz-latn.js":"010e","./uz.js":"2e8c","./vi":"2921","./vi.js":"2921","./x-pseudo":"fd7e","./x-pseudo.js":"fd7e","./yo":"7f33","./yo.js":"7f33","./zh-cn":"5c3a","./zh-cn.js":"5c3a","./zh-hk":"49ab","./zh-hk.js":"49ab","./zh-mo":"3a6c","./zh-mo.js":"3a6c","./zh-tw":"90ea","./zh-tw.js":"90ea"};function a(e){var t=o(e);return n(t)}function o(e){if(!n.o(c,e)){var t=new Error("Cannot find module '"+e+"'");throw t.code="MODULE_NOT_FOUND",t}return c[e]}a.keys=function(){return Object.keys(c)},a.resolve=o,e.exports=a,a.id="4678"},"56d7":function(e,t,n){"use strict";n.r(t);n("e260"),n("e6cf"),n("cca6"),n("a79d"),n("d3b7"),n("3ca3"),n("ddb0"),n("2b3d"),n("ac1f"),n("841c"),n("99af");var c=n("2b0e"),a=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("v-app",[n("v-main",[e.hasTokens?[n("router-view",{key:e.$route.fullPath})]:[n("v-row",{attrs:{justify:"center"}},[n("v-col",{attrs:{md:"6"}},[n("v-alert",{attrs:{prominent:"",type:"error"}},[n("v-row",{attrs:{align:"center"}},[n("v-col",{staticClass:"grow"},[e._v(" Error ")])],1)],1)],1)],1)]],2)],1)},o=[],r=n("5530"),i=n("2f62"),s=n("4360"),u={name:"App",data:function(){return{}},computed:Object(r["a"])({},Object(i["c"])({hasTokens:"api/hasTokens"})),methods:Object(r["a"])({},Object(i["b"])({refreshToken:"api/refreshToken"})),created:function(){s["a"].state.api.api_token&&setInterval(function(){this.refreshToken()}.bind(this),354e4)},mounted:function(){s["a"].state.api.api_token&&this.refreshToken()}},p=u,d=(n("5c0b"),n("2877")),l=n("6544"),f=n.n(l),h=n("0798"),m=n("7496"),b=n("62ad"),g=n("f6c4"),v=n("0fd9"),j=Object(d["a"])(p,a,o,!1,null,null,null),k=j.exports;f()(j,{VAlert:h["a"],VApp:m["a"],VCol:b["a"],VMain:g["a"],VRow:v["a"]});var x=n("8c4f"),y=function(){var e=this,t=e.$createElement,c=e._self._c||t;return!e.experiments||e.experiments.length<1?c("v-container",[c("v-row",{staticClass:"text-center",attrs:{justify:"center"}},[c("v-col",{staticClass:"mt-15 ",attrs:{md:"6"}},[c("v-img",{staticClass:"mb-13 mx-auto",attrs:{src:n("1f92"),alt:"Terracotta Logo","max-width":"173"}}),c("h1",[e._v("Experimental research in the LMS")]),c("p",{staticClass:"mb-10"},[e._v(" Welcome to Terracotta, the platform that supports teachers' and researchers' abilities to easily run experiments in live classes. "),c("br"),e._v(" New to Terracotta? "),c("a",{attrs:{href:"https://terracotta.education/terracotta-overview",target:"_blank"}},[e._v("Read an overview of the tool")]),e._v(". ")]),c("p",{staticClass:"mb-0"},[e._v("Ready to get started?")]),c("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:e.startExperiment}},[e._v("Create your first experiment")])],1)],1)],1):c("v-container",[c("v-row",{staticClass:"mb-5",attrs:{justify:"space-between"}},[c("v-col",{attrs:{cols:"6"}},[c("v-img",{attrs:{src:n("1f92"),alt:"Terracotta Logo","max-width":"138"}})],1),c("v-col",{staticClass:"text-right",attrs:{cols:"6"}},[c("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:e.startExperiment}},[e._v("New Experiment")])],1)],1),c("v-row",[c("v-col",{attrs:{cols:"12"}},[c("h1",{staticClass:"pl-4 mb-3"},[e._v("Experiments")]),c("v-data-table",{attrs:{headers:e.headers,items:e.experiments},scopedSlots:e._u([{key:"item.title",fn:function(t){var n=t.item;return[n?c("router-link",{staticClass:"v-data-table__link",attrs:{to:{name:"ExperimentDesignIntro",params:{experiment_id:n.experimentId}}}},[n.title?[e._v(" "+e._s(n.title)+" ")]:[c("em",[e._v("No Title")])]],2):e._e()]}},{key:"item.updatedAt",fn:function(t){var n=t.item;return[n.updatedAt?c("span",[e._v(e._s(e._f("formatDate")(n.updatedAt)))]):e._e()]}},{key:"item.actions",fn:function(t){var n=t.item;return[c("v-menu",{attrs:{"offset-y":""},scopedSlots:e._u([{key:"activator",fn:function(t){var n=t.on,a=t.attrs;return[c("v-icon",e._g(e._b({attrs:{color:"black"}},"v-icon",a,!1),n),[e._v(" mdi-dots-horizontal ")])]}}],null,!0)},[c("v-list",{attrs:{dense:""}},[c("v-list-item",{on:{click:function(t){return e.handleDelete(n)}}},[c("v-list-item-icon",{staticClass:"mr-3"},[c("v-icon",{attrs:{color:"black"}},[e._v("mdi-delete")])],1),c("v-list-item-content",[c("v-list-item-title",[e._v("Delete")])],1)],1)],1)],1)]}}])})],1)],1)],1)},_=[],S=n("c1df"),E=n.n(S),T={name:"Home",data:function(){return{headers:[{text:"Experiment name",value:"title"},{text:"Last modified",value:"updatedAt"},{text:"Actions",value:"actions",sortable:!1}]}},filters:{formatDate:function(e){return E()(e).fromNow()}},computed:Object(r["a"])({},Object(i["c"])({experiments:"experiment/experiments"})),methods:Object(r["a"])(Object(r["a"])({},Object(i["b"])({fetchExperiments:"experiment/fetchExperiments",createExperiment:"experiment/createExperiment",deleteExperiment:"experiment/deleteExperiment"})),{},{handleExport:function(){},handleDelete:function(e){null!==e&&void 0!==e&&e.experimentId&&confirm('Do you really want to delete "'.concat(e.title,'"?'))&&this.deleteExperiment(e.experimentId).then((function(e){200!==(null===e||void 0===e?void 0:e.status)&&(alert("Could not delete experiment."),console.log({response:e}))}))},startExperiment:function(){var e=this;this.createExperiment().then((function(t){null!==t&&void 0!==t&&t.experimentId?e.$router.push({name:"ExperimentDesignIntro",params:{experiment_id:t.experimentId}}):alert("There was an issue creating an experiment")})).catch((function(e){console.log("startExperiment -> createExperiment | catch",{response:e})}))}}),created:function(){this.fetchExperiments()}},C=T,O=(n("21bb"),n("8336")),w=n("a523"),I=n("8fea"),P=n("132d"),A=n("adda"),D=n("8860"),z=n("da13"),R=n("5d23"),L=n("34c3"),N=n("e449"),V=Object(d["a"])(C,y,_,!1,null,null,null),B=V.exports;f()(V,{VBtn:O["a"],VCol:b["a"],VContainer:w["a"],VDataTable:I["a"],VIcon:P["a"],VImg:A["a"],VList:D["a"],VListItem:z["a"],VListItemContent:R["a"],VListItemIcon:L["a"],VListItemTitle:R["b"],VMenu:N["a"],VRow:v["a"]}),c["a"].use(x["a"]);var J=[{path:"/",name:"Home",component:B},{path:"/experiment/:experiment_id",component:function(){return n.e("chunk-2d0ba0bb").then(n.bind(null,"3613"))},children:[{path:"",alias:"design",component:function(){return n.e("chunk-4257b527").then(n.bind(null,"7f79"))},meta:{currentSection:"design",currentStep:"design"},children:[{path:"",alias:"intro",name:"ExperimentDesignIntro",component:function(){return n.e("chunk-2d207d1a").then(n.bind(null,"a1c7"))},meta:{currentSection:"design",currentStep:"design"}},{path:"title",name:"ExperimentDesignTitle",component:function(){return n.e("chunk-7c2b03ee").then(n.bind(null,"d301"))},meta:{currentSection:"design",currentStep:"design_title"}},{path:"description",name:"ExperimentDesignDescription",component:function(){return n.e("chunk-16a4f436").then(n.bind(null,"9d27"))},meta:{currentSection:"design",currentStep:"design_description"}},{path:"conditions",name:"ExperimentDesignConditions",component:function(){return n.e("chunk-f7d7c106").then(n.bind(null,"8217"))},meta:{currentSection:"design",currentStep:"design_conditions"}},{path:"type",name:"ExperimentDesignType",component:function(){return n.e("chunk-1af076e8").then(n.bind(null,"d97a"))},meta:{currentSection:"design",currentStep:"design_type"}},{path:"default-condition",name:"ExperimentDesignDefaultCondition",component:function(){return n.e("chunk-e2add046").then(n.bind(null,"9075"))},meta:{currentSection:"design",currentStep:"design_type"}},{path:"summary",name:"ExperimentDesignSummary",component:function(){return n.e("chunk-a40b083a").then(n.bind(null,"8ae1"))},meta:{currentSection:"design",currentStep:"design_type",stepsComplete:!0}}]},{path:"participation",component:function(){return n.e("chunk-4257b527").then(n.bind(null,"7f79"))},meta:{currentSection:"participation"},children:[{path:"",alias:"intro",name:"ExperimentParticipationIntro",component:function(){return n.e("chunk-2d0e5d09").then(n.bind(null,"95a8"))},meta:{currentSection:"participation",currentStep:"participation"}},{path:"selection-method",name:"ExperimentParticipationSelectionMethod",component:function(){return n.e("chunk-7eb255ce").then(n.bind(null,"a2eb"))},meta:{currentSection:"participation",currentStep:"participation_selection_method"}},{path:"participation-type-auto-confirm",name:"ParticipationTypeAutoConfirm",component:function(){return n.e("chunk-6ff53ed8").then(n.bind(null,"4193"))},meta:{selectionType:"auto",currentSection:"participation",currentStep:"participation_selection_method"}},{path:"participation-type-consent/intro",name:"ParticipationTypeConsentOverview",component:function(){return n.e("chunk-2d20f689").then(n.bind(null,"b401"))},meta:{selectionType:"consent",currentSection:"participation",currentStep:"participation_selection_method"}},{path:"participation-type-consent/title",name:"ParticipationTypeConsentTitle",component:function(){return n.e("chunk-2d216fd2").then(n.bind(null,"c58b"))},meta:{selectionType:"consent",currentSection:"participation",currentStep:"participation_selection_consent_title"}},{path:"participation-type-consent/file",name:"ParticipationTypeConsentFile",component:function(){return n.e("chunk-602f794d").then(n.bind(null,"6b4c"))},meta:{selectionType:"consent",currentSection:"participation",currentStep:"participation_selection_consent_file"}},{path:"manual-selection",name:"ParticipationTypeManual",component:function(){return n.e("chunk-7c0220d4").then(n.bind(null,"a60e"))},meta:{selectionType:"manual",currentSection:"participation",currentStep:"participation_selection_method"}},{path:"manual-participant-selection",name:"ParticipationTypeManualSelection",component:function(){return n.e("chunk-530c7014").then(n.bind(null,"00d9"))},meta:{selectionType:"manual",currentSection:"participation",currentStep:"select_participants"}},{path:"participant-distribution",name:"ParticipationDistribution",component:function(){return n.e("chunk-ef639f40").then(n.bind(null,"0ef2"))},meta:{selectionType:"any",currentSection:"participation",currentStep:"participation_distribution"}},{path:"participation-summary",name:"ParticipationSummary",component:function(){return n.e("chunk-1c52ac5c").then(n.bind(null,"bec2"))},meta:{currentSection:"participation",currentStep:"select_participants"}}]},{path:"assignments",component:function(){return n.e("chunk-4257b527").then(n.bind(null,"7f79"))},meta:{currentSection:"assignments"},children:[{path:"",alias:"intro",name:"AssignmentIntro",component:function(){return n.e("chunk-2d22c525").then(n.bind(null,"f379"))},meta:{currentSection:"assignments",currentStep:"assignment_intro"}},{path:"exposure-sets",name:"AssignmentExposureSets",component:function(){return n.e("chunk-6dbac39e").then(n.bind(null,"5100"))},meta:{currentSection:"assignments",currentStep:"assignment_intro"}},{path:"exposure-sets/intro",name:"AssignmentExposureSetsIntro",component:function(){return n.e("chunk-7c39e644").then(n.bind(null,"e3f2"))},meta:{currentSection:"assignments",currentStep:"assignment_intro"}},{path:"create-assignment",name:"AssignmentCreateAssignment",component:function(){return n.e("chunk-748d27d7").then(n.bind(null,"4ff92"))},meta:{currentSection:"assignments",currentStep:"your_assignments"}}]},{path:"summary",name:"ExperimentSummary",component:function(){return n.e("chunk-2d0d7488").then(n.bind(null,"75c7"))}}]}],G=new x["a"]({base:"/app/",routes:J}),M=G,F=(n("5363"),n("f309"));c["a"].use(F["a"]);var U=new F["a"]({icons:{iconfont:"mdi"},theme:{themes:{light:{primary:"#1D9DFF"}}}}),$=n("688f"),q=n.n($);c["a"].config.productionTip=!1,c["a"].use(q.a,{router:M});var H=new URL(window.location.href),K=new URLSearchParams(H.search),W=K.get("token");function Q(){X(),new c["a"]({store:s["a"],router:M,vuetify:U,render:function(e){return e(k)}}).$mount("#app")}function X(){K.delete("token"),window.history.replaceState({},"","".concat(window.location.pathname,"?").concat(K).concat(window.location.hash))}W?s["a"].dispatch("api/setLtiToken",W).then(Q):Q()},"5c0b":function(e,t,n){"use strict";n("9c0c")},"9c0c":function(e,t,n){}});
//# sourceMappingURL=app.a4d9aeb3.js.map