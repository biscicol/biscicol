var labelType, useGradients, nativeTextSupport, animate;
var loading = "<center><img src='/images/gif-loading.gif'></center>";
(function() {
        // TODO: figure out this error
      //iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
      typeOfCanvas = typeof HTMLCanvasElement,
      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
      textSupport = nativeCanvasSupport
        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
  //I'm setting this based on the fact that ExCanvas provides text support for IE
  //and that as of today iPhone/iPad current text support is lame
  //labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
  //nativeTextSupport = labelType == 'Native';
  //useGradients = nativeCanvasSupport;
  //animate = !(iStuff || !nativeCanvasSupport);
})();

var Log = {
  elem: false,
  write: function(text){
    if (!this.elem)
      this.elem = document.getElementById('log');
    this.elem.innerHTML = text;
    this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
  }
};


// initialize script
function init(){
    $('#infovis-canvaswidget').remove();
    $jit.id('inner-details').innerHTML = "";

    /*
    var guid = $("#guid").val();
    var queryRadio = $('input[name=querytype]');
    var querytype = queryRadio.filter(':checked').val();
    var graph = $("#graph").val();
    var url = "/rest/JiT/?guid=" + guid + "&querytype=" + querytype + "&graph=" + graph;
    */
    var url = "/rest/JiT/?" + $("#hypertreeForm").serialize();

    var json = "";

    $.ajax({
        url: url,
	    dataType: "json",
	    success: function(data) {
            if (data) {
                populateTree(data);
            }
        },
        always: function() {
            $("#status").html("");
        },
        complete: function() {
            $("#status").html("");
        },
        beforeSend: function (xhr) {
	        $("#status").html(loading);
        },
        fail: function() {
            $("#status").html("Failed loading data...");
        }
    });
}

// pass JSON from init script, this populates the trees and information containers
function populateTree(json) {
    //end
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth - 50, h = infovis.offsetHeight - 50;

    //init Hypertree
    var ht = new $jit.RGraph({
      background: {
            CanvasStyles: {
            strokeStyle: '#555'
        }
      },
      //id of the visualization container
      injectInto: 'infovis',
      //canvas width and height
      width: w,
      height: h,
      //Change node and edge styles such as
      //color, width and dimensions.
      Node: {
          dim: 9,
          color: "#f00"
      },
      Edge: {
          lineWidth: 2,
          color: "#088"
      },
      onBeforeCompute: function(node){
          Log.write("centering");
      },
      //Attach event handlers and add text to the
      //labels. This method is only triggered on label
      //creation
      onCreateLabel: function(domElement, node){
          domElement.innerHTML = node.name;
          $jit.util.addEvent(domElement, 'click', function () {
              ht.onClick(node.id, {
                  onComplete: function() {
                      ht.controller.onComplete();
                  }
              });
          });
      },
      //Change node styles when labels are placed
      //or moved.
      onPlaceLabel: function(domElement, node){
          var style = domElement.style;
          style.display = '';
          style.cursor = 'pointer';
          if (node._depth <= 1) {
              style.fontSize = "0.8em";
              style.color = "#ddd";

          } else if(node._depth == 2){
              style.fontSize = "0.7em";
              style.color = "#555";

          } else {
              style.display = 'none';
          }

          var left = parseInt(style.left);
          var w = domElement.offsetWidth;
          style.left = (left - w / 2) + 'px';
      },

      onComplete: function(){
          Log.write("done");

          //Build the right column relations list.
          //This is done by collecting the information (stored in the data property)
          //for all the nodes adjacent to the centered node.
          var node = ht.graph.getClosestNodeToOrigin("current");

          var html = "<h4>" + node.name + "</h4>";
          html += "<p><b>Attributes:</b>";
            html += "<ul>";
          for (var key in node.data) {
                if (key != "relation" &&
                    key != "$span" &&
                    key != "$dim-quotient" &&
                    key != "sameAs")
                        html += "<li><div class=\"relation\">" + key  + " = " + node.data[key] + "</div></li>";
          }
            html += "</ul>";

          html += "<p><b>Connections:</b>";
          // Display child nodes
          html += "<ul>";
          node.eachAdjacency(function(adj){
              var child = adj.nodeTo;
              if (child.data) {
                    // Only display connections
                    if (child.data.relation) {
                        html += "<li><div class=\"relation\">" + child.name + " (" + child.data.relation + ")</div></li>";
                    }
              }
          });
          html += "</ul>";
          $jit.id('inner-details').innerHTML = html;
      }
    });
    //load JSON data.
    ht.loadJSON(json);
    //compute positions and plot.
    ht.refresh();
    //end
    ht.controller.onComplete();
}