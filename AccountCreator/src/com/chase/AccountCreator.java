package com.chase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import com.anti_captcha.Api.NoCaptcha;
import com.anti_captcha.Api.NoCaptchaProxyless;
import com.anti_captcha.Helper.DebugHelper;

public class AccountCreator {

	public static final String API_KEY = "INSERT_API_KEY_HERE";
	public static final String WEBSITE_URL = "https://secure.runescape.com/m=account-creation/g=oldscape/create_account?trialactive=true";
	public static final String WEBSITE_KEY = "6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b";

	public static void main(String[] args) throws InterruptedException, MalformedURLException {

		Scanner in = new Scanner(System.in);
		AccountCreator creator = new AccountCreator();

		System.out.print("IP : ");
		String ip = in.next();

		System.out.print("PORT : ");
		int port = in.nextInt();

		System.out.print("DISPLAY NAME : ");
		String displayName = in.next();

		System.out.print("EMAIL : ");
		String email = in.next();

		System.out.print("PASSWORD : ");
		String password = in.next();

		System.out.print("AGE : ");
		int age = in.nextInt();

		in.close();

		creator.getBalance();
		creator.createAccount(ip, port, displayName, email, password, age);
	}

	public boolean createAccount(String ip, int port, String displayName, String email, String password, int age)
			throws MalformedURLException, InterruptedException {

		String captchaSolution = solveCaptchaWithProxy(ip, port);
		if (captchaSolution == null) {
			return false;
		}

		String[][] postVars = { { "trialactive", "true" }, { "onlyOneEmail", "1" }, { "displayNamePresent", "true" },
				{ "age", String.valueOf(age) }, { "displayname", displayName }, { "email1", email },
				{ "password1", password }, { "password2", password }, { "g-recaptcha-response", captchaSolution },
				{ "submit", "Join Now" } };

		try {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
			URL postURL = new URL(WEBSITE_URL);
			URLConnection postURLConnect = postURL.openConnection(proxy);
			postURLConnect.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(postURLConnect.getOutputStream());
			for (String[] postVar : postVars) {
				out.write(postVar[0] + "=" + postVar[1] + "&");
			}
			out.flush();

			// Get the response
			BufferedReader br = new BufferedReader(new InputStreamReader(postURLConnect.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Account Created")) {
					DebugHelper.out("Account was successfully created.", DebugHelper.Type.SUCCESS);
					return true;
				}
			}

			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			DebugHelper.out("Failed to make the account create request!", DebugHelper.Type.ERROR);
		}

		return false;
	}

	public boolean createAccount(String displayName, String email, String password, int age)
			throws MalformedURLException, InterruptedException {

		String captchaSolution = solveCaptchaProxyless();
		if (captchaSolution == null) {
			return false;
		}

		String[][] postVars = { { "trialactive", "true" }, { "onlyOneEmail", "1" }, { "displayNamePresent", "true" },
				{ "age", String.valueOf(age) }, { "displayname", displayName }, { "email1", email },
				{ "password1", password }, { "password2", password }, { "g-recaptcha-response", captchaSolution },
				{ "submit", "Join Now" } };

		try {
			URL postURL = new URL(WEBSITE_URL);
			URLConnection postURLConnect = postURL.openConnection();
			postURLConnect.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(postURLConnect.getOutputStream());
			for (String[] postVar : postVars) {
				out.write(postVar[0] + "=" + postVar[1] + "&");
			}
			out.flush();

			// Get the response
			BufferedReader br = new BufferedReader(new InputStreamReader(postURLConnect.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Account Created")) {
					DebugHelper.out("Account was successfully created.", DebugHelper.Type.SUCCESS);
					return true;
				}
			}

			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			DebugHelper.out("Failed to make the account create request!", DebugHelper.Type.ERROR);
		}

		return false;
	}

	public Double getBalance() {
		DebugHelper.setVerboseMode(true);

		NoCaptcha api = new NoCaptcha();
		api.setClientKey(API_KEY);

		Double balance = api.getBalance();

		if (balance == null) {
			DebugHelper.out("GetBalance() failed. " + api.getErrorMessage(), DebugHelper.Type.ERROR);
		} else {
			DebugHelper.out("Balance: " + balance, DebugHelper.Type.SUCCESS);
		}

		return balance;
	}

	private String solveCaptchaProxyless() throws MalformedURLException, InterruptedException {
		DebugHelper.setVerboseMode(true);

		NoCaptchaProxyless api = new NoCaptchaProxyless();
		api.setClientKey(API_KEY);
		api.setWebsiteUrl(new URL(WEBSITE_URL));
		api.setWebsiteKey(WEBSITE_KEY);

		if (!api.createTask()) {
			DebugHelper.out("API v2 send failed. " + api.getErrorMessage(), DebugHelper.Type.ERROR);
		} else if (!api.waitForResult()) {
			DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
		} else {
			String solution = api.getTaskSolution();

			DebugHelper.out("Result: " + solution, DebugHelper.Type.SUCCESS);
			return solution;
		}

		return null;
	}

	private String solveCaptchaWithProxy(String ip, int port) throws MalformedURLException, InterruptedException {
		DebugHelper.setVerboseMode(true);

		NoCaptcha api = new NoCaptcha();
		api.setClientKey(API_KEY);
		api.setWebsiteUrl(new URL(WEBSITE_URL));
		api.setWebsiteKey(WEBSITE_KEY);
		api.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 "
				+ "(KHTML, like Gecko) Chrome/52.0.2743.116");

		// proxy access parameters
		api.setProxyType(NoCaptcha.ProxyTypeOption.HTTP);
		api.setProxyAddress(ip);
		api.setProxyPort(port);
		// api.setProxyLogin("login");
		// api.setProxyPassword("password");

		if (!api.createTask()) {
			DebugHelper.out("API v2 send failed. " + api.getErrorMessage(), DebugHelper.Type.ERROR);
		} else if (!api.waitForResult()) {
			DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
		} else {
			String solution = api.getTaskSolution();

			DebugHelper.out("Result: " + solution, DebugHelper.Type.SUCCESS);
			return solution;
		}

		return null;
	}

}