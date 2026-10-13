package simplysip;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a page in a REAL browser (Chromium, driven by Playwright) so the page's JavaScript runs,
 * then hands the finished HTML to Jsoup. Everything after that (MenuFinder, Extractors,
 * DrinkFilter) works exactly as before, because it still just receives a Jsoup Document.
 * The first time it runs, Playwright downloads Chromium (about 150 MB). That needs internet once.
 */
public final class BrowserLoader implements AutoCloseable {

    /** false opens a visible browser window so you can watch what it sees */
    static final boolean HEADLESS = true;

    /** how long to wait for a page to start loading, in milliseconds. */
    static final int PAGE_TIMEOUT_MS = 45_000;

    /** extra time after scrolling for late-loading content, in milliseconds. */
    static final int EXTRA_WAIT_MS = 1500;

    /** save the rendered HTML and any JSON the page downloaded into the output/ folder, to see what the browser saw. */
    static final boolean SAVE_DEBUG_FILES = true;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;

    /** JSON responses the pages downloaded while loading. */
    private final List<String> jsonBodies = new ArrayList<>();
    private int pagesLoaded = 0;

    /** Every JSON response seen so far. MenuFinder.fromJson() searches these for menu items. */
    List<String> capturedJson() { return jsonBodies; }

    private void start() {
        if (playwright != null) return;
        System.out.println("  [browser] starting Playwright (the first run downloads Chromium and may show no progress for a minute or two)...");
        playwright = Playwright.create();                                     // first run downloads the browser
        System.out.println("  [browser] Playwright started, launching Chromium...");
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(HEADLESS));
        // We identify ourselves honestly (same USER_AGENT as the Jsoup requests). We do NOT try to hide that this is a bot.
        context = browser.newContext(new Browser.NewContextOptions().setUserAgent(Main.USER_AGENT));
        context.onResponse(r -> {                                             // remember JSON the page fetches
            try {
                String type = r.headers().getOrDefault("content-type", "");
                if (r.status() != 200 || !type.contains("json") || jsonBodies.size() >= 300) return;
                String body = r.text();
                if (body.length() < 3_000_000 && body.length() > 20) {
                    jsonBodies.add(body);
                    if (SAVE_DEBUG_FILES && jsonBodies.size() <= 40) saveDebug("debug-json-" + jsonBodies.size() + ".json", "// " + r.url() + "\n" + body);
                }
            } catch (PlaywrightException ignored) { /* redirect, closed page, or body not available */ }
        });
        System.out.println("  [browser] Chromium is ready.");
    }

    /** Opens the URL, waits for the JavaScript to build the page, and returns the result as a Jsoup Document. */
    Document load(String url) throws IOException {
        try {
            start();
            Page page = context.newPage();
            System.out.println("  [browser] loading " + url);
            try {
                page.navigate(url, new Page.NavigateOptions()
                        .setTimeout(PAGE_TIMEOUT_MS).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                try {                                                             // wait until the network goes quiet...
                    page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(15_000));
                } catch (TimeoutError ignored) { /* ...but some sites never go quiet; carry on anyway */ }
                scrollDown(page);                                                 // many menus load more items as you scroll
                page.waitForTimeout(EXTRA_WAIT_MS);

                StringBuilder html = new StringBuilder(page.content());
                // Some menus (Toast, Square, ...) live inside an <iframe>. Append each iframe's HTML so Jsoup can see it too.
                for (Frame f : page.frames()) {
                    if (f.parentFrame() == null) continue;                          // skip the main page itself
                    try { html.append("<div data-iframe=\"").append(f.url().replace("\"", "")).append("\">").append(f.content()).append("</div>"); }
                    catch (PlaywrightException ignored) { /* this frame closed or isn't readable */ }
                }
                Document doc = Jsoup.parse(html.toString(), page.url());
                pagesLoaded++;
                if (SAVE_DEBUG_FILES && pagesLoaded <= 5) saveDebug("debug-page-" + pagesLoaded + ".html", doc.outerHtml());
                System.out.println("  [browser] done: page now has " + doc.body().text().length() + " characters of text"
                        + (page.title().isBlank() ? "" : " (title: " + page.title() + ")"));
                return doc;
            } finally {
                page.close();
            }
        } catch (PlaywrightException e) {
            String msg = e.getMessage() == null ? e.toString() : e.getMessage().split("\\R")[0];
            throw new IOException("browser error: " + msg, e);
        }
    }

    /** Scrolls the whole page, then any inner scrolling panel too (many ordering sites scroll a div, not the window). */
    private static void scrollDown(Page page) {
        page.evaluate("""
        async () => {
          const sleep = ms => new Promise(r => setTimeout(r, ms));
          for (let i = 0; i < 40 && i * 700 < document.body.scrollHeight; i++) {
            window.scrollTo(0, i * 700);
            await sleep(200);
          }
          window.scrollTo(0, 0);
          const panels = [...document.querySelectorAll('*')].filter(e => {
            const s = getComputedStyle(e);
            return (s.overflowY === 'auto' || s.overflowY === 'scroll') && e.scrollHeight > e.clientHeight + 50;
          });
          for (const el of panels.slice(0, 10)) {
            for (let y = 0; y < el.scrollHeight && y < 30000; y += 600) {
              el.scrollTop = y;
              await sleep(150);
            }
            el.scrollTop = 0;
          }
        }
        """);
    }

    /** Writes a file into the output/ folder. */
    private static void saveDebug(String fileName, String content) {
        try {
            Files.createDirectories(Path.of("output"));
            Files.writeString(Path.of("output", fileName), content);
        } catch (IOException ignored) { }
    }

    @Override public void close() {
        try { if (context != null) context.close(); } catch (Exception ignored) { }
        try { if (browser != null) browser.close(); } catch (Exception ignored) { }
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) { }
    }
}