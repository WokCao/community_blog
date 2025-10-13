(function () {
    function pxToNumber(px) {
        const n = parseFloat(px);
        return isNaN(n) ? 0 : n;
    }

    function measureTagWidth(el) {
        const style = getComputedStyle(el);
        const ml = pxToNumber(style.marginLeft);
        const mr = pxToNumber(style.marginRight);
        return el.offsetWidth + ml + mr;
    }

    function createMorePill(count) {
        const more = document.createElement("span");
        more.className =
            "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border border-gray-200 text-gray-600 bg-gray-100 cursor-pointer transition-colors hover:bg-gray-200 relative";
        more.textContent = `+${count}`;
        more.setAttribute("data-more-pill", "1");
        return more;
    }

    function layoutContainer(container) {
        if (!container) return;

        // Remove previous +N
        const prevMore = container.querySelector('[data-more-pill="1"]');
        if (prevMore) prevMore.remove();

        const tags = Array.from(container.querySelectorAll(":scope > .tag-pill"));
        if (!tags.length) return;

        // Show all before measuring
        tags.forEach((t) => (t.style.display = ""));

        const containerWidth = container.clientWidth;
        const containerStyle = getComputedStyle(container);
        const gapX = pxToNumber(containerStyle.columnGap || containerStyle.gap);

        let totalWidth = 0;
        tags.forEach((t, i) => {
            totalWidth += measureTagWidth(t);
            if (i > 0) totalWidth += gapX;
        });
        if (totalWidth <= containerWidth) return;

        // Prepare +N placeholder
        const tempMore = createMorePill(99);
        tempMore.style.visibility = "hidden";
        container.appendChild(tempMore);
        const moreWidth = measureTagWidth(tempMore);

        // Determine how many tags fit
        let used = 0;
        let fitCount = 0;
        for (let i = 0; i < tags.length; i++) {
            const w = measureTagWidth(tags[i]);
            const addGap = i > 0 ? gapX : 0;
            const nextUsed = used + addGap + w;
            const withMore = nextUsed + gapX + moreWidth;
            if (withMore <= containerWidth) {
                used = nextUsed;
                fitCount++;
            } else break;
        }

        const hiddenCount = tags.length - fitCount;
        if (fitCount === 0) {
            tags.forEach((t) => (t.style.display = "none"));
            tempMore.style.visibility = "";
            tempMore.textContent = `+${hiddenCount}`;
            return;
        }

        // Hide overflow
        tags.forEach((t, i) => (t.style.display = i < fitCount ? "" : "none"));

        tempMore.style.visibility = "";
        tempMore.textContent = `+${hiddenCount}`;
    }

    function layoutAll() {
        document.querySelectorAll(".tag-row").forEach(layoutContainer);
    }

    function rafLayoutAll() {
        requestAnimationFrame(layoutAll);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", () => {
            rafLayoutAll();
            setTimeout(rafLayoutAll, 150);
            window.addEventListener("resize", rafLayoutAll);
        });
    } else {
        rafLayoutAll();
        setTimeout(rafLayoutAll, 150);
        window.addEventListener("resize", rafLayoutAll);
    }

    window.addEventListener("load", () => {
        setTimeout(rafLayoutAll, 200);
    });
})();
