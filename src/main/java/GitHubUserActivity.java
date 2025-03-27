import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GitHubUserActivity {
  public static void main(String[] args) {
    String GIT_HUB_URL = "https://api.github.com/users/%s/events";
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter the GitHub username: ");
    String name = scanner.nextLine();
    hitGitHubUrl(GIT_HUB_URL, name);
  }

  private static void hitGitHubUrl(String GIT_HUB_URL, String name) {
    String formattedUrl = String.format(GIT_HUB_URL, name);
    try {
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(formattedUrl)).build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 404) {
        System.out.println("User not found. Please check the username.");
        return;
      }
      if (response.statusCode() == 200) {
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        displayActivity(jsonArray);
      } else {
        System.out.println("Error:" + response.statusCode());
      }
    } catch (IOException uriSyntaxException) {
      uriSyntaxException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      interruptedException.printStackTrace();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  private static void displayActivity(JsonArray events) {
    for (JsonElement element : events) {
      JsonObject event = element.getAsJsonObject();
      String type = event.get("type").getAsString();
      String action = switch (type) {
        case "PushEvent" -> {
          int commitCount = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray().size();
          yield "Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
        }
        case "IssuesEvent" -> event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                + event.get("payload").getAsJsonObject().get("action").getAsString()
                + " an issue in ${event.repo.name}";
        case "WatchEvent" -> "Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
        case "ForkEvent" -> "Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
        case "CreateEvent" -> "Created " + event.get("payload").getAsJsonObject().get("ref_type").getAsString()
                + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
        default -> event.get("type").getAsString().replace("Event", "")
                + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
      };
      System.out.println(action);
    }
  }
}
