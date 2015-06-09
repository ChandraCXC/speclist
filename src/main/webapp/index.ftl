<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>JS Bin</title>
    <link rel="stylesheet" href="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.css" />
    <script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
    <script type="text/javascript" src="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.js"></script>
    <script src="http://code.highcharts.com/highcharts.js"></script>
    <script src="http://code.highcharts.com/modules/exporting.js"></script>
    <script src="http://code.highcharts.com/highcharts-more.js"></script>
</head>
<body>
<br/>
<p>Examples:
<ul>
    <li>
        http://dc.g-vo.org/flashheros/q/sdl/dlget?ID=ivo%3A//org.gavo.dc/%7E%3Fflashheros/data/ca92/f0006.mt&FORMAT=application/x-votable%2Bxml%3Bcontent%3Dspec2
    </li>
    <li>
        http://dc.g-vo.org/theossa/q/sdl/dlget?ID=ivo%3A%2F%2Fwww.g-vo.org%2Ftheossa%2Fq%2Fdata%2F0038000_5.70_H_9.968E-01_HE_3.167E-03_02000-03000A_2008-08-02_07_20_01&FORMAT=application/x-votable%2Bxml%3Bcontent%3Dspec2
    </li>
</ul>
</p>
<form name="dsForm" method="POST" action="/">
    URL: <input type='text' name='ds' size='100'/>
    prefix: <input type='text' name='prefix' value='spec'/>
    <a href="javascript: submitform()">Go</a>
</form>
<script type="text/javascript">
function submitform()
{
  document.dsForm.submit();
}
</script>
<br/>
<br/>
<button id="toggle-btn">Toggle</button>

<div id="metadata"></div>
<div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>

<script>
var metadata = ${metadata}

    $(function() {
    $('#metadata').JSONView(metadata, { collapsed: true });

    $('#toggle-btn').on('click', function() {
    $('#metadata').JSONView('toggle');
    });

    });

var data = ${data}

function zip(arrays) {
    return arrays[0].map(function(_,i){
        return arrays.map(function(array){return array[i]})
    });
}

var xy = zip([data.x, data.y]);

var err = function() {
    nonzeros = data.yErrLow.filter(function(x) { if (x != 0) {return x;} });
    nonzeros.concat(data.yErrHigh.filter(function(x) { if (x != 0) {return x;} }));
    if (nonzeros.length) {
        return zip([data.x, data.yErrLow, data.yErrHigh]);
    } else {
        return zip([data.x, "NaN", "NaN"]);
    }
}

$(function () {
    $('#container').highcharts({
        chart: {
            zoomType: 'xy',
        },
        title: {
            text: 'Spectrum'
        },
        subtitle: {
            text: data.target
        },
        tooltip: {
            crosshairs: [true, true],
            formatter: function() {
                return '<b>'+data.target+'</b><br/> <b>x:</b> '
                + this.x.toExponential() + ' '
                +data.xUnits+'<br/><b>y:</b> ' + this.y.toExponential()+' '+data.yUnits;
            }
        },
        xAxis: {
            type: "linear",
            title: {
                text: 'Spectral Axis ('+data.xUnits+')'
            },
            labels: {
                formatter : function() {
                    return this.value.toExponential();
                }
            },
        },
        yAxis: {
            title: {
                text: 'FluxAxis ('+data.yUnits+')'
            },
            labels: {
                formatter : function() {
                    return this.value.toExponential();
                }
            }
        },

        plotOptions: {
            area: {
                fillColor: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                },
                marker: {
                    radius: 2
                },
                lineWidth: 1,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            },
            spline: {
                marker: {
                    enabled: true
                }
            }
        },

        series: [{
            type: 'area',
            data: xy
        },
        {
            name: 'Error',
            type: 'errorbar',
            data: err,
            tooltip: {
                pointFormat: '(error range: {point.low}-{point.high})<br/>'
            }
        },
        ]
    });
});

</script>
</body>
</html>