package tw.com.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtil2 {
	private static final Logger logger = LogManager.getLogger(XMLUtil2.class);

	private static String getTypeConvertVal(String type, String value) {
		Object object = null;
		java.util.Date utilDate = null;
		java.sql.Date sqlDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

		switch (type) {
		case "String":
			object = String.valueOf(value);
			break;
		case "BigDecimal":
			object = new BigDecimal(value);
			break;
		case "boolean":
			object = Boolean.valueOf(value);
			break;
		case "byte":
			object = Byte.valueOf(value);
			break;
		case "short":
			object = Short.valueOf(value);
			break;
		case "int":
			object = Integer.valueOf(value);
			break;
		case "long":
			object = Long.valueOf(value);
			break;
		case "float":
			object = Float.valueOf(value);
			break;
		case "double":
			object = Double.valueOf(value);

			break;
		case "byte[]":
			byte[] b = value.getBytes(StandardCharsets.UTF_8);
			object = b;
			break;
		case "Date":

			try {
				utilDate = sdf.parse(value);
				sqlDate = new java.sql.Date(utilDate.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			object = sqlDate;
			break;
		case "Time":
			java.sql.Time time = null;
			try {
				utilDate = sdf.parse(value);
				time = new java.sql.Time(utilDate.getTime());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			object = time;
			break;
		case "Timestamp":
			Timestamp timestamp = null;
			try {
				utilDate = sdf.parse(value);
				timestamp = new java.sql.Timestamp(utilDate.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			object = timestamp;
			break;
		}
		return value = object.toString();
	}

	public static Document getDocument(String path) {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(path);
		Document document = null;
		try {
			document = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		return document;
	}

	public static void delete(String configPath, String convertPath) {

		// 得到自定義的設定物件
		Document configDoc = getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();

		// 得到要進行轉換的物件
		Document convertDoc = getDocument(convertPath);

		// 從config XML中撈取刪除相關資訊
		NodeList delete = configRoot.getElementsByTagName("Delete");

		// 拿到要進行轉換的XML所有節點
		NodeList root = convertDoc.getElementsByTagName("*");

		NodeList insertList = null, table = null;
		Map<String, String> map = null;
		StringBuffer sBuffer = new StringBuffer();
		String tableName = null, sqlStr = null;

		for (int i = 0; i < delete.getLength(); i++) {
			Node delete_node = (Node) delete.item(i);
			if ("Delete".equals(delete_node.getNodeName())) {
				insertList = delete_node.getChildNodes();
				for (int j = 0; j < insertList.getLength(); j++) {
					Node insert_node = (Node) insertList.item(j);
					if ("Table".equals(insert_node.getNodeName())) {
						NamedNodeMap namedNodeMap = insert_node.getAttributes();

						table = insert_node.getChildNodes();

						for (int k = 0; k < namedNodeMap.getLength(); k++) {
							Node attr = namedNodeMap.item(k);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();
							if ("name".equals(attrName)) {
								tableName = attrVal;
							}
						}
						for (int l = 0; l < table.getLength(); l++) {
							Node field = (Node) table.item(l);
							if (field.getNodeType() == Node.ELEMENT_NODE) {
								map = new HashMap<String, String>();
							}
							NodeList fieldInfo = field.getChildNodes();
							for (int m = 0; m < fieldInfo.getLength(); m++) {
								Node node = (Node) fieldInfo.item(m);
								if (node.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = node.getNodeName();
									String value = node.getTextContent();
									if ("Source".equals(nodeName)) {
										map.put("source", value);
									}
									if ("Destination".equals(nodeName)) {
										map.put("destination", value);
									}
									if ("Type".equals(nodeName)) {
										map.put("type", value);
									}
								}
							}
						}

						for (int n = 0; n < root.getLength(); n++) {

							Element element = (Element) root.item(n);
							if (element.getNodeType() == Node.ELEMENT_NODE) {
								String nodeName = element.getNodeName();

								if (nodeName.equals(map.get("source"))) {

									String destination = map.get("destination");
									String value = element.getTextContent();
									String type = map.get("type");

									type = Util.getFieldType(type);
									value = getTypeConvertVal(type, value);

									sBuffer.append("DELETE FROM ").append(tableName).append(" WHERE ")
											.append(destination).append(" = \"").append(value).append("\"; ");
									sqlStr = sBuffer.toString();
									System.out.println("===============================");
									logger.debug(sqlStr);
									System.out.println("===============================");

									sqlExec(configDoc,sqlStr);
									sBuffer.setLength(0);
								}
							}

						}

					}
				}
			}
		}
	}

	private static void sqlExec(Document configDoc,String sqlStr) {

		Element configRoot = configDoc.getDocumentElement();

		// 從config XML中撈取資料庫連線所需資訊
		NodeList databaseConnectionFactory = configRoot.getElementsByTagName("databaseConnectionFactory");
		NodeList databaseInfo = databaseConnectionFactory.item(0).getChildNodes();

		String jdbcDriver = null;
		String dbURL = null;
		String dbUserName = null;
		String dbPassword = null;

		for (int i = 0; i < databaseInfo.getLength(); i++) {
			Node node = (Node) databaseInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				jdbcDriver = nodeName.equals("jdbcDriver") ? value : jdbcDriver;
				dbURL = nodeName.equals("dbURL") ? value : dbURL;
				dbUserName = nodeName.equals("dbUserName") ? value : dbUserName;
				dbPassword = nodeName.equals("dbPassword") ? value : dbPassword;
			}
		}
		logger.debug("jdbcDriver: {} \\ dbURL: {} \\ dbUserName: {} \\ dbPassword: {}", jdbcDriver, dbURL, dbUserName,
				dbPassword);

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName(jdbcDriver);
			con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);

			pstmt = con.prepareStatement(sqlStr);

			pstmt.execute();

		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("A database error occured. " + cnfe.getMessage());
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException se) {
				logger.error("SQLException:".concat(se.getMessage()));
			} catch (Exception e) {
				logger.error("Exception:".concat(e.getMessage()));
			}
		}
	}

	public static void insert(String configPath, String convertPath) {

		// 得到自定義的設定物件
		Document configDoc = getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();

		// 得到要進行轉換的物件
		Document convertDoc = getDocument(convertPath);

		String sqlStr = null;

		// 從config XML中撈取新增相關資訊
		String tableName = null;
		NodeList insert = configRoot.getElementsByTagName("Insert");
		NodeList insertList = null;
		NodeList table = null;
		for (int i = 0; i < insert.getLength(); i++) {
			Node node = (Node) insert.item(i);
			if ("Insert".equals(node.getNodeName())) {
				insertList = node.getChildNodes();
				for (int j = 0; j < insertList.getLength(); j++) {
					Node insert_node = (Node) insertList.item(j);
					if ("Table".equals(insert_node.getNodeName())) {
						NamedNodeMap namedNodeMap = insert_node.getAttributes();
						for (int l = 0; l < namedNodeMap.getLength(); ++l) {
							Node attr = namedNodeMap.item(l);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();
							if ("name".equals(attrName)) {
								tableName = attrVal;
							}
						}
						table = insert_node.getChildNodes();
					}
				}
			}
		}

		List<Map<String, String>> configList = new ArrayList<>();

		Map<String, String> map = null;
		for (int i = 0; i < table.getLength(); i++) {
			Node field = (Node) table.item(i);
			if (field.getNodeType() == Node.ELEMENT_NODE) {
				map = new HashMap<String, String>();
			}
			NodeList fieldInfo = field.getChildNodes();
			for (int j = 0; j < fieldInfo.getLength(); j++) {
				Node node = (Node) fieldInfo.item(j);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String value = node.getTextContent();
					if ("Source".equals(nodeName)) {
						map.put("source", value);
					}
					if ("Destination".equals(nodeName)) {
						map.put("destination", value);
					}
					if ("Type".equals(nodeName)) {
						map.put("type", value);
					}
				}
				if (j == fieldInfo.getLength() - 1) {
					configList.add(map);
				}
			}
		}
		logger.debug(configList);

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("INSERT INTO ");
		sBuffer.append(tableName);
		sBuffer.append(" (");
		// 拿到要進行轉換的XML所有節點
		NodeList root = convertDoc.getElementsByTagName("*");
		Map<String, String> convertMap = new HashMap<>();

		for (int i = 0; i < root.getLength(); i++) {

			Element element = (Element) root.item(i);
			if (element.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = element.getNodeName();

				for (int j = 0; j < configList.size(); j++) {
					Map<String, String> config = configList.get(j);
					if (nodeName.equals(config.get("source"))) {

						String destination = config.get("destination");
						sBuffer.append(destination);
						sBuffer.append(",");
					}
				}
			}

		}
		sBuffer.setLength(sBuffer.length() - 1);
		sBuffer.append(") VALUES (");

		for (int i = 0; i < root.getLength(); i++) {

			Element element = (Element) root.item(i);
			if (element.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = element.getNodeName();

				for (int j = 0; j < configList.size(); j++) {
					Map<String, String> config = configList.get(j);
					if (nodeName.equals(config.get("source"))) {

						String value = element.getTextContent();
						String type = config.get("type");
						String destination = config.get("destination");


						type = Util.getFieldType(type);
						value = getTypeConvertVal(type, value);

						sBuffer.append("\"");
						sBuffer.append(value);
						sBuffer.append("\",");
					}
				}
			}

		}
		sBuffer.setLength(sBuffer.length() - 1);
		sBuffer.append(");");
		sqlStr = sBuffer.toString();
		logger.debug(sqlStr);


		sqlExec(configDoc,sqlStr);
	}

	// public static void xmlToTable(Document doc) throws SQLException
	//
	// {
	//
	// Connection con = null;
	//
	// try {
	// Class.forName("com.mysql.jdbc.Driver");
	// con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ian",
	// "root", "root");
	// } catch (Exception e) {
	// System.out.println(e);
	// System.exit(0);
	// }
	//
	// System.out.println("Table Name= " +
	// doc.getElementsByTagName("TableName").item(0).getTextContent());
	//
	// StringBuffer ddl = new StringBuffer(
	// "create table " +
	// doc.getElementsByTagName("TableName").item(0).getTextContent() + "1 (");
	//
	// StringBuffer dml = new StringBuffer(
	// "insert into " +
	// doc.getElementsByTagName("TableName").item(0).getTextContent() + "1 (");
	// System.out.println("ddl: " + ddl.toString());
	// System.out.println("dml: " + dml.toString());
	// NodeList tableStructure = doc.getElementsByTagName("TableStructure");
	//
	// int no_of_columns = tableStructure.item(0).getChildNodes().getLength();
	//
	// System.out.println("no_of_columns: " +
	// doc.getElementsByTagName("Column").getLength());
	//
	// for (int i = 0; i < no_of_columns; i++) {
	// ddl.append(doc.getElementsByTagName("ColumnName").item(i).getTextContent()
	// + " "
	// + doc.getElementsByTagName("ColumnType").item(i).getTextContent() + "("
	// + doc.getElementsByTagName("Length").item(i).getTextContent() + "),");
	// dml.append(doc.getElementsByTagName("ColumnName").item(i).getTextContent()
	// + ",");
	//
	// }
	//
	// for (int i = 0; i < no_of_columns; i++) {
	// ddl.append(doc.getElementsByTagName("ColumnName").item(i).getTextContent()
	// + " "
	// + doc.getElementsByTagName("ColumnType").item(i).getTextContent() + "("
	// + doc.getElementsByTagName("Length").item(i).getTextContent() + "),");
	// dml.append(doc.getElementsByTagName("ColumnName").item(i).getTextContent()
	// + ",");
	//
	// }
	//
	// System.out.println(" DDL " + ddl.toString());
	// System.out.println(" dml " + dml.toString());
	//
	// ddl = ddl.replace(ddl.length() - 1, ddl.length(), ")");
	// dml = dml.replace(dml.length() - 1, dml.length(), ") values(");
	//
	// System.out.println(" DDL " + ddl.toString());
	//
	// for (int k = 0; k < no_of_columns; k++)
	// dml.append("?,");
	//
	// dml = dml.replace(dml.length() - 1, dml.length(), ")");
	//
	// System.out.println(" dml " + dml.toString());
	//
	// Statement stmt = null;
	//
	// try {
	// stmt = con.createStatement();
	// // to create table One time only;
	// stmt.executeUpdate(ddl.toString());
	//
	// } catch (Exception e) {
	// System.out.println("Tables already created, skipping table creation
	// process" + e.toString());
	// }
	//
	// NodeList tableData = doc.getElementsByTagName("TableData");
	//
	// int tdlen = tableData.item(0).getChildNodes().getLength();
	//
	// PreparedStatement prepStmt = con.prepareStatement(dml.toString());
	//
	// String colName = "";
	// for (int i = 0; i < tdlen; i++) {
	// System.out.println("Outer" + i);
	//
	// for (int j = 0; j < tableStructure.item(0).getChildNodes().getLength();
	// j++) {
	//
	// colName =
	// doc.getElementsByTagName("ColumnName").item(j).getTextContent();
	// prepStmt.setString(j + 1,
	// doc.getElementsByTagName(colName).item(i).getTextContent());
	//
	// System.out.println("Data =" +
	// doc.getElementsByTagName(colName).item(i).getTextContent());
	//
	// }
	//
	// prepStmt.addBatch();
	//
	// }
	//
	// int[] numUpdates = prepStmt.executeBatch();
	//
	// System.out.println(numUpdates + " records inserted");
	//
	// }
	public static Map<String, Object> XML2Map(String xml) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}

		InputSource is = null;

		try {
			is = new InputSource(new StringReader(xml));
		} catch (Exception e) {
		}
		Document doc = null;
		try {
			doc = dombuilder.parse(is);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}

		NodeList all_nodeList = doc.getElementsByTagName("*");
		StringBuilder textContent = new StringBuilder();
		for (int i = 0; i < all_nodeList.getLength(); i++) {

			Element element = (Element) all_nodeList.item(i);

			if (element.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = element.getNodeName();

				map.put(nodeName, null);

				if (i == 0)
					map.put("XmlType", element.getNodeName());

				textContent.append(nodeName);

				NodeList nodeList = element.getChildNodes();

				for (int j = 0; j < nodeList.getLength(); j++) {
					Node node = (Node) nodeList.item(j);
					if (node.getNodeType() == Node.TEXT_NODE) {
						String text = node.getTextContent();
						textContent.append(" " + text);
						map.put(nodeName, text);

					}
				}

				NamedNodeMap element_attr = element.getAttributes();

				for (int j = 0; j < element_attr.getLength(); ++j) {
					Node attr = element_attr.item(j);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					map.put(attrName, attrVal);
				}
			}
			textContent.append("\n");
		}
		System.out.println(textContent.toString());

		return map;

	}
}
