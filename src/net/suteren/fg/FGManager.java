package net.suteren.fg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.suteren.domain.DayMenu;
import net.suteren.domain.Food;
import net.suteren.domain.Live;
import net.suteren.domain.Pasta;
import net.suteren.domain.Soup;
import net.suteren.domain.Superior;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class FGManager {

	Locker locker;

	public FGManager(Locker locker) {
		this.locker = locker;
	}

	public Node retrieve(URL url, InputStream inXsl) throws IOException,
			TransformerException, ParserConfigurationException, LockedException {
		if (locker.isLocked())
			throw new LockedException();
		HttpURLConnection con = download(url);
		Document d = tidy(con.getInputStream(), con.getContentEncoding());
		DOMResult res = transform(d, inXsl);
		return res.getNode();
	}

	private HttpURLConnection download(URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		return con;
	}

	private DOMResult transform(Document d, InputStream inXsl)
			throws IOException, TransformerConfigurationException,
			TransformerFactoryConfigurationError, ParserConfigurationException,
			TransformerException {
		Transformer tr = TransformerFactory.newInstance().newTransformer(
				new StreamSource(inXsl));
		DOMResult res = new DOMResult(DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument());
		tr.transform(new DOMSource(d), res);
		return res;
	}

	private Document tidy(InputStream is, String enc) throws IOException {
		Tidy t = new Tidy();
		t.setInputEncoding(enc == null ? "cp1250" : enc);
		t.setNumEntities(true);
		t.setXmlOut(true);
		t.setShowWarnings(false);
		t.setTrimEmptyElements(true);
		// t.setQuoteNbsp(true);
		Document d = t.parseDOM(is, null);
		is.close();
		return d;
	}

	public void save(FileOutputStream fos, Node n)
			throws FileNotFoundException, TransformerException,
			TransformerConfigurationException,
			TransformerFactoryConfigurationError, IOException {
		try {
			locker.lock();
			TransformerFactory.newInstance().newTransformer()
					.transform(new DOMSource(n), new StreamResult(fos));
			fos.close();
		} finally {
			locker.unlock();
		}
	}

	public void load(FileInputStream s, SortedSet<DayMenu> days)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, ParseException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document n = db.parse(s);

		XPath xp = XPathFactory.newInstance().newXPath();
		XPathExpression xpDate = xp.compile("/menu/@time");
		String timeString = xpDate.evaluate(n);

		String[] dates = timeString.split("\\s*-\\s*");

		DateFormat df = new SimpleDateFormat("d.M.y");
		Date from = df.parse(dates[0]);
		XPathExpression xpDay = xp.compile("/menu/day");
		NodeList dayNodes = (NodeList) xpDay
				.evaluate(n, XPathConstants.NODESET);

		for (int j = 0; j < dayNodes.getLength(); j++) {
			Node dayNode = dayNodes.item(j);

			NamedNodeMap attrs = dayNode.getAttributes();
			String position = attrs.getNamedItem("position").getNodeValue();

			Calendar cal = Calendar.getInstance();
			cal.setTime(from);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.DATE, Integer.parseInt(position) - 1);
			DayMenu dm = new DayMenu(cal);

			NodeList foodNodes = dayNode.getChildNodes();
			for (int k = 0; k < foodNodes.getLength(); k++) {

				Node fn = foodNodes.item(k);
				attrs = fn.getAttributes();
				Node type = attrs.getNamedItem("type");
				String value = type.getNodeValue();

				if ("soup".equals(value)) {
					dm.addSoup(new Soup(fn.getTextContent()));
				} else if ("normal".equals(value)) {
					dm.addFood(new Food(fn.getTextContent()));
				} else if ("live".equals(value)) {
					dm.addLive(new Live(fn.getTextContent()));
				} else if ("superior".equals(value)) {
					dm.addSuperior(new Superior(fn.getTextContent()));
				} else if ("pasta".equals(value)) {
					dm.addPasta(new Pasta(fn.getTextContent()));
				}

			}
			days.add(dm);
		}
	}
}
