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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class DemoServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = request.getParameter("ds");
        String prefix = request.getParameter("prefix");
        SpectralDataset ds = getDatasetForURL(url, prefix);
        Genson genson = make_genson();
        String meta = genson.serialize(ds);
        String data = genson.serialize(ds, SimpleSpectrum.class);

        request.setAttribute("metadata", meta);
        request.setAttribute("data", data);

        request.getRequestDispatcher("index.ftl").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
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
