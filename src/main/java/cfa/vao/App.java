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

import java.io.*;
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
    public static void main( String[] args ) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            HttpContext context = server.createContext("/test", new Handler());
            context.getFilters().add(new ParameterFilter());
            server.setExecutor(null);
            server.start();
        } catch (Throwable ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            try {
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
            } catch (Throwable ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                t.sendResponseHeaders(500, 0);
                OutputStream os = t.getResponseBody();
                Writer w = new OutputStreamWriter(os);
                ex.printStackTrace(new PrintWriter(w));
                w.close();
                os.close();
            }
        }
    }

    public static SpectralDataset getDatasetForURL(String url, String prefix) throws IOException {
        if (url == null || url.isEmpty()) {
            return (SpectralDataset) new ModelObjectFactory().newInstance(SpectralDataset.class);
        }

        URL file = new URL(url);

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
