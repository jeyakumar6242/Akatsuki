package trading.api.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import trading.api.entity.NseEntity;
import trading.api.utility.http.HttpDeleteWithEntity;
import trading.api.utility.http.HttpGetWithEntity;

@Service
public class TradingUtility {

	private String base64SessionToken;
	private String secretKey;
	private String apiKey;

	public Config config;

	public void errorException(String message) throws Exception {
		throw new Exception(message);
	}

	public JSONObject validationResponse(String payload, int status, String message) throws JSONException {
		return new JSONObject() {
			{
				put("Success", payload);
				put("Status", status);
				put("Error", message);
			}
		};
	}

	public void setSession(String apiKey, String secretKey, String base64SessionToken) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.base64SessionToken = base64SessionToken;
		config = new Config();
	}

	public String currentTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date(System.currentTimeMillis())) + ".000Z";
	}

	public static String checksumValue(JSONObject jsonData, String timeStamp, String secretKey) throws JSONException {
		String hexData = timeStamp + jsonData.toString() + secretKey;
		return DigestUtils.sha256Hex(hexData);
	}

	public JSONArray generateHeaders(JSONObject body) {
		try {
			String timestamp = currentTimestamp();
			JSONArray headersObject = new JSONArray();
			headersObject.put("token " + checksumValue(body, timestamp, this.secretKey));
			headersObject.put(timestamp);
			return headersObject;
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String makeRequest(String method, String endpoint, JSONObject requestBody, JSONArray headers) {
		try {
			int CONNECTION_TIMEOUT_MS = 10000; // Timeout in millis.
			CloseableHttpClient client = HttpClients.createDefault();
			HttpEntityEnclosingRequestBase http = null;
			switch (method) {
			case "GET":
				http = new HttpGetWithEntity();
				break;
			case "POST":
				http = new HttpPost();
				break;
			case "PUT":
				http = new HttpPut();
				break;
			case "DELETE":
				http = new HttpDeleteWithEntity();
				break;
			default:
				this.errorException(config.exceptionMessage.get(Config.ExceptionEnum.INVALID_REQUEST_EXCEPTION));
			}
			http.setConfig(RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
					.setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build());
			http.setURI(URI.create(config.urls.get(Config.UrlEnum.API_URL) + endpoint));
			http.setEntity(new StringEntity(requestBody.toString()));
			http.setHeader("Content-type", "application/json");
			http.setHeader("X-Checksum", headers.getString(0));
			http.setHeader("X-Timestamp", headers.getString(1));
			http.setHeader("X-AppKey", this.apiKey);
			http.setHeader("X-SessionToken", this.base64SessionToken);
			String responseString = "";
			try {
				CloseableHttpResponse response = client.execute(http);
				HttpEntity responseEntity = response.getEntity();
				responseString = EntityUtils.toString(responseEntity, "UTF-8");
			} finally {
				client.close();
			}
			return responseString;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public NseEntity getHistoricalData(String interval, String fromDate, String toDate, String stockCode,
			String exchangeCode, String productType, String expiryDate, String right, String strikePrice) throws JsonMappingException, JsonProcessingException {
		try {

			/*
			 * if (interval.isBlank() || interval.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_INTERVAL)); } else if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.INTERVAL_TYPES)))
			 * .contains(interval.toLowerCase())) { return this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.INTERVAL_TYPE_ERROR)); } else
			 * if (exchangeCode.isBlank() || exchangeCode.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_EXCHANGE_CODE)); } else
			 * if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.EXCHANGE_CODES_HIST)))
			 * .contains(exchangeCode.toLowerCase())) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.EXCHANGE_CODE_ERROR)); }
			 * else if (fromDate.isBlank() || fromDate.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_FROM_DATE)); } else if
			 * (toDate.isBlank() || toDate.isEmpty()) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.BLANK_TO_DATE)); } else
			 * if (stockCode.isBlank() || stockCode.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_STOCK_CODE)); } else if
			 * (exchangeCode.equalsIgnoreCase("nfo")) { if (productType.isBlank() ||
			 * productType.isEmpty()) { return this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.PRODUCT_TYPE_ERROR_NFO)); }
			 * else if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.PRODUCT_TYPES_HIST)))
			 * .contains(productType.toLowerCase())) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.PRODUCT_TYPE_ERROR_NFO));
			 * } else if (productType.equalsIgnoreCase("options")) { if
			 * (strikePrice.isBlank() || strikePrice.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_STRIKE_PRICE)); } else
			 * if (right.isBlank() || right.isEmpty()) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.BLANK_RIGHT_TYPE)); }
			 * else if (!(Arrays.asList(config.typeLists.get(Config.ListEnum.RIGHT_TYPES)))
			 * .contains(right.toLowerCase())) { return this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.RIGHT_TYPE_ERROR)); } } else
			 * if (expiryDate.isBlank() || expiryDate.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_EXPIRY_DATE)); } }
			 */

			if (interval.equalsIgnoreCase("1minute")) {
				interval = "minute";
			} else if (interval.equalsIgnoreCase("1day")) {
				interval = "day";
			}
			JSONObject body = new JSONObject();
			body.put("interval", interval);
			body.put("from_date", fromDate);
			body.put("to_date", toDate);
			body.put("stock_code", stockCode);
			body.put("exchange_code", exchangeCode);
			if (!(productType.isBlank() || productType.isEmpty())) {
				body.put("product_type", productType);
			}
			if (!(expiryDate.isBlank() || expiryDate.isEmpty())) {
				body.put("expiry_date", expiryDate);
			}
			if (!(strikePrice.isBlank() || strikePrice.isEmpty())) {
				body.put("strike_price", strikePrice);
			}
			if (!(right.isBlank() || right.isEmpty())) {
				body.put("right", right);
			}
			JSONArray headers = generateHeaders(body);
			String response = makeRequest(config.apiMethods.get(Config.APIMethodEnum.GET),
					config.endPoints.get(Config.EndPointEnum.HIST_CHART), body, headers);
			NseEntity nseEntity = new ObjectMapper().readValue(response, NseEntity.class);
		//	System.out.println(response);
			return nseEntity;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public NseEntity getHistoricalDatav2(String interval, String fromDate, String toDate, String stockCode,
			String exchangeCode, String productType, String expiryDate, String right, String strikePrice) {
		try {
			/*
			 * if (interval.isBlank() || interval.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_INTERVAL)); } else if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.INTERVAL_TYPES_HIST_V2)
			 * )) .contains(interval.toLowerCase())) { return this.validationResponse("",
			 * 500,
			 * config.responseMessage.get(Config.ResponseEnum.INTERVAL_TYPE_ERROR_HIST_V2));
			 * } else if (exchangeCode.isBlank() || exchangeCode.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_EXCHANGE_CODE)); } else
			 * if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.EXCHANGE_CODES_HIST_V2)
			 * )) .contains(exchangeCode.toLowerCase())) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.EXCHANGE_CODE_HIST_V2_ERROR));
			 * } else if (fromDate.isBlank() || fromDate.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_FROM_DATE)); } else if
			 * (toDate.isBlank() || toDate.isEmpty()) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.BLANK_TO_DATE)); } else
			 * if (stockCode.isBlank() || stockCode.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_STOCK_CODE)); } else if
			 * (exchangeCode.equalsIgnoreCase("nfo") || exchangeCode.equalsIgnoreCase("ndx")
			 * || exchangeCode.equalsIgnoreCase("mcx")) { if (productType.isBlank() ||
			 * productType.isEmpty()) { return this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_PRODUCT_TYPE_HIST_V2));
			 * } else if
			 * (!(Arrays.asList(config.typeLists.get(Config.ListEnum.PRODUCT_TYPES_HIST_V2))
			 * ) .contains(productType.toLowerCase())) { return this.validationResponse("",
			 * 500,
			 * config.responseMessage.get(Config.ResponseEnum.PRODUCT_TYPE_ERROR_HIST_V2));
			 * } else if (productType.equalsIgnoreCase("options")) { if
			 * (strikePrice.isBlank() || strikePrice.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_STRIKE_PRICE)); } else
			 * if (right.isBlank() || right.isEmpty()) { return this.validationResponse("",
			 * 500, config.responseMessage.get(Config.ResponseEnum.BLANK_RIGHT_TYPE)); }
			 * else if (!(Arrays.asList(config.typeLists.get(Config.ListEnum.RIGHT_TYPES)))
			 * .contains(right.toLowerCase())) { return this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.RIGHT_TYPE_ERROR)); } } else
			 * if (expiryDate.isBlank() || expiryDate.isEmpty()) { return
			 * this.validationResponse("", 500,
			 * config.responseMessage.get(Config.ResponseEnum.BLANK_EXPIRY_DATE)); } }
			 */

			String query_params = "?";
			query_params = (query_params + "interval=" + interval);
			query_params = (query_params + "&from_date=" + fromDate);
			query_params = (query_params + "&to_date=" + toDate);
			query_params = (query_params + "&stock_code=" + stockCode);
			query_params = (query_params + "&exch_code=" + exchangeCode);
			if (!(productType.isBlank() || productType.isEmpty())) {
				query_params = (query_params + "&product_type=" + productType);
			}
			if (!(expiryDate.isBlank() || expiryDate.isEmpty())) {
				query_params = (query_params + "&expiry_date=" + expiryDate);
			}
			if (!(strikePrice.isBlank() || strikePrice.isEmpty())) {
				query_params = (query_params + "&strike_price=" + strikePrice);
			}
			if (!(right.isBlank() || right.isEmpty())) {
				query_params = (query_params + "&right=" + right);
			}

			int CONNECTION_TIMEOUT_MS = 10000; // Timeout in millis.
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet http = null;

			String requestURL = config.urls.get(Config.UrlEnum.BREEZE_NEW_URL)
					+ config.endPoints.get(Config.EndPointEnum.HIST_CHART) + query_params;
			http = new HttpGet();
			http.setConfig(RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
					.setConnectTimeout(CONNECTION_TIMEOUT_MS).setCookieSpec(CookieSpecs.STANDARD)
					.setSocketTimeout(CONNECTION_TIMEOUT_MS).build());
			http.setURI(URI.create(requestURL));
			http.setHeader("Content-type", "application/json");
			http.setHeader("apikey", this.apiKey);
			http.setHeader("X-SessionToken", this.base64SessionToken);
			String responseString = "";
			try {
				CloseableHttpResponse response = client.execute(http);
				HttpEntity responseEntity = response.getEntity();
				responseString = EntityUtils.toString(responseEntity, "UTF-8");
			} catch (ClientProtocolException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				client.close();
			}
			System.out.println(responseString);

			NseEntity nseEntity = new ObjectMapper().readValue(responseString, NseEntity.class);
			
			return nseEntity;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
