package cfa.vao;

import cfa.vo.speclib.Quantity;
import cfa.vo.speclib.SpectralDataset;
import cfa.vo.speclib.doc.ModelObjectFactory;
import cfa.vo.speclib.io.VOTableIO;
import cfa.vo.vomodel.DefaultModelBuilder;
import cfa.vo.vomodel.Model;
import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        HttpContext context = server.createContext("/test", new Handler());
        context.getFilters().add(new ParameterFilter());
        server.setExecutor(null);
        server.start();
    }

    private static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Map<String, Object> params = (Map<String, Object>) t.getAttribute("parameters");
            String url = (String) params.get("ds");
            String prefix = (String) params.get("prefix");
            SpectralDataset ds = getDatasetForURL(url, prefix);
            Genson genson = make_genson();
            String meta = genson.serialize(ds);
            String data = genson.serialize(ds, SimpleSpectrum.class);

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
            cfg.setClassForTemplateLoading(App.class, "/");
            Template template = cfg.getTemplate("page.html");
            Map dict = new HashMap<String, Object>();
            dict.put("metadata", meta);
            dict.put("data", data);

            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            Writer w = new OutputStreamWriter(os);
            try {
                template.process(dict, w);
                w.close();
            } catch (TemplateException e) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
//            os.write(response.getBytes());
            os.close();
        }
    }

    public static SpectralDataset getDatasetForURL(String url, String prefix) throws IOException {
        if (url == null || url.isEmpty()) {
            return (SpectralDataset) new ModelObjectFactory().newInstance(SpectralDataset.class);
        }

        URL file = new URL(url);
//        URL file = new URL("http://dc.g-vo.org/flashheros/q/sdl/dlget?ID=ivo%3A//org.gavo.dc/%7E%3Fflashheros/data/ca92/f0006.mt&FORMAT=application/x-votable%2Bxml%3Bcontent%3Dspec2");
//        URL file = new URL("file:///Users/olaurino/Downloads/result.vot");

        VOTableIO instance = new VOTableIO();
        Model model = new DefaultModelBuilder("Spectrum-2.0")
                .withPrefix(prefix == null || prefix.isEmpty()? "spec" : prefix)
                .build();

        return instance.read(file, model);
    }

    private static class DoubleConverter implements Converter<Double> {

        public void serialize(Double number, ObjectWriter writer, Context ctx) throws Exception {

            if (Double.isNaN(number)) {
                writer.writeString("NaN");
            } else if (Double.POSITIVE_INFINITY == number) {
                writer.writeString("+Inf");
            } else if (Double.NEGATIVE_INFINITY ==number) {
                writer.writeString("-Inf");
            } else {
                writer.writeNumber(number);
            }

        }

        public Double deserialize(ObjectReader objectReader, Context context) throws Exception {
            return null;
        }
    }

        private static Genson make_genson() {
        return new GensonBuilder()
                .exclude("data", SpectralDataset.class)
                .exclude("description", Quantity.class)
                .exclude("name", Quantity.class)
                .exclude("utype", Quantity.class)
                .exclude("UCD", Quantity.class)
                .exclude("ID")
                .rename("sNR", "SNR")
                .rename("uRL", "URL")
                .rename("uCD", "UCD")
                .exclude("setID")
                .exclude("setIdentifier")
                .exclude("setModelPath")
                .exclude("setName")
                .exclude("setDescription")
                .exclude("setUCD")
                .exclude("setUnit")
                .exclude("setUtype")
                .exclude("setValue")
                .exclude("setDataProductType")
                .exclude("setDataProductSubtype")
                .exclude("setCalibLevel")
                .exclude("setDataModel")
                .exclude("setCuration")
                .exclude("setCharacterization")
                .exclude("setCoordSys")
                .exclude("setDataID")
                .exclude("setDerived")
                .exclude("setObsConfig")
                .exclude("setProposal")
                .exclude("setTarget")
                .exclude("setData")
                .exclude("setFluxSI")
                .exclude("setSpactralSI")
                .exclude("setTimeSI")
                .exclude("setRedshift")
                .exclude("setSNR")
                .exclude("setVarAmpl")
                .exclude("setConfidence")
                .exclude("setStatError")
                .exclude("setPrefix")
                .exclude("setURL")
                .exclude("setCollections")
                .exclude("setContributors")
                .exclude("setCreationType")
                .exclude("setCreator")
                .exclude("setCreatorDID")
                .exclude("setDatasetID")
                .exclude("setDate")
                .exclude("setLogo")
                .exclude("setObservationID")
                .exclude("setVersion")
                .exclude("setTitle")
                .exclude("setContact")
                .exclude("setEmail")
                .exclude("setPublisher")
                .exclude("setPublisherDID")
                .exclude("setPublisherID")
                .exclude("setReferences")
                .exclude("setReleaseDate")
                .exclude("setRights")
                .exclude("setCharacterizationAxes")
                .exclude("setEquinox")
                .exclude("setReferencePosition")
                .exclude("setZero")
                .exclude("setDopplerDefinition")
                .exclude("setSpectralSI")
                .exclude("setObservingElements")
                .exclude("setIdentified")
                .exclude("modelpath")
                .exclude("length")
                .useIndentation(true)
                .useRuntimeType(true)
                .useBeanViews(true)
                .setSkipNull(true)
                .withConverter(new DoubleConverter(), Double.class)
                .create();
    }
}
