package com.starfish.kol.model.models.login;

import com.starfish.kol.connection.PartialServerReply;
import com.starfish.kol.connection.ServerReply;
import com.starfish.kol.connection.Session;
import com.starfish.kol.gamehandler.GameHandler;
import com.starfish.kol.gamehandler.ViewContext;
import com.starfish.kol.model.Model;
import com.starfish.kol.request.Request;
import com.starfish.kol.request.ResponseHandler;
import com.starfish.kol.request.SingleRequest;
import com.starfish.kol.util.Regex;

public class LoginModel extends Model<LoginStatus> {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -8728194484440083488L;

	private static final Regex SERVER = new Regex("^appserver=(.*)$", 1);

	private static final Regex LOGIN_ID = new Regex(
			".*/login.php\\?loginid=(.*)", 1);
	private static final Regex CHALLENGE = new Regex(
			"<input type=hidden name=challenge value=\"([^\"]*?)\">", 1);

	public LoginModel() {
		super(new Session());
	}

	public void cheat(ViewContext context) {
		Request req = new Request("static.php?id=whatiskol", new GameHandler(
				context));
		this.makeRequest(req);
	}

	public void login(final ViewContext context, final String username,
			final PasswordHash hash) {
		this.notifyView(LoginStatus.STARTING);

		Request req = new Request("login.php", new ResponseHandler() {
			@Override
			public void handle(Session session, Request request,
					PartialServerReply response) {
				ServerReply fullResponse = response.complete();
				if(fullResponse == null)
					return;

				String loginId = LOGIN_ID.extractSingle(fullResponse.url);
				String challenge = CHALLENGE.extractSingle(fullResponse.html);
				String server = SERVER.extractSingle(fullResponse.cookie);

				if (loginId == null || challenge == null || server == null) {
					notifyView(LoginStatus.FAILED_ACCESS);
					return;
				}

				session.setCookie(fullResponse.cookie);
				String[] names = { "loginid", "loginname", "password",
						"loggingin", "challenge", "response", "secure" };
				String[] vals = { loginId, username, "", "Yup.", challenge,
						hash.completeChallenge(challenge), "1" };
				notifyView(LoginStatus.HALFWAY);

				Request login = new SingleRequest("login.php", names, vals,
						new ResponseHandler() {

							@Override
							public void handle(Session session,
									Request request, PartialServerReply response) {
								System.out.println("Logincookie: "
										+ response.cookie);
								if (!response.cookie.contains("PHPSESSID=")) {
									// Failure to login
									notifyView(LoginStatus.FAILED_LOGIN);
									return;
								}

								notifyView(LoginStatus.SUCCESS);

								session.setCookie(response.cookie);
								Request game = new Request("main.php",
										new GameHandler(context));
								game.makeAsync(session);
							}

						});

				login.makeAsync(session);
			}
		});
		this.makeRequest(req);
	}
}
