package net.suteren.fg;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.suteren.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import android.content.res.Resources;

public class FgFetcher {

	public Node getFgMenu() throws IOException, TransformerException,
			ParserConfigurationException {
		// TODO Auto-generated method stub
		URL url = new URL(
				"http://fgkavcihory.cateringmelodie.cz/cz/samoobsluzna-restaurace/denni-menu-tisk.php?kolikaty_tyden=44&zvoleny_den=1");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		InputStream is = con.getInputStream();
		String enc = con.getContentEncoding();
		Tidy t = new Tidy();
		t.setInputEncoding(enc == null ? "cp1250" : enc);
		t.setNumEntities(true);
		t.setXmlOut(true);
		t.setShowWarnings(false);
		t.setTrimEmptyElements(true);
		// t.setQuoteNbsp(true);
		t.setErrout(new PrintWriter(new StringWriter()));

		Document d = t.parseDOM(is, null);
		TransformerFactory tf = TransformerFactory.newInstance();
		InputStream inXsl = Resources.getSystem().openRawResource(R.xml.fg);
		StreamSource xslStream = new StreamSource(inXsl);
		Transformer tr = tf.newTransformer(xslStream);
		DOMResult res = new DOMResult(DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument());
		tr.transform(new DOMSource(d), res);
		return res.getNode();
	}

}
