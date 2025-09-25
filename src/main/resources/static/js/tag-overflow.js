(function () {
  function pxToNumber(px) {
    const n = parseFloat(px);
    return isNaN(n) ? 0 : n;
  }

  function measureTagWidth(el) {
    // offsetWidth includes border and padding, not margins
    const style = getComputedStyle(el);
    const ml = pxToNumber(style.marginLeft);
    const mr = pxToNumber(style.marginRight);
    return el.offsetWidth + ml + mr;
  }

  function createMorePill(count) {
    const more = document.createElement('span');
    more.className = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border border-gray-200 text-gray-600 bg-gray-100';
    more.textContent = `+${count}`;
    more.setAttribute('data-more-pill', '1');
    return more;
  }

  function layoutContainer(container) {
    if (!container) return;

    // Remove any previous "+N" pill
    const prevMore = container.querySelector('[data-more-pill="1"]');
    if (prevMore) prevMore.remove();

    const tags = Array.from(container.querySelectorAll(':scope > .tag-pill'));
    if (!tags.length) return;

    // Make sure all are visible before measuring
    tags.forEach((t) => (t.style.display = ''));

    const containerWidth = container.clientWidth; // excludes scrollbars
    const containerStyle = getComputedStyle(container);
    const gapX = pxToNumber(containerStyle.columnGap || containerStyle.gap);

    // Early exit: if everything fits, we're done
    let totalWidth = 0;
    tags.forEach((t, i) => {
      totalWidth += measureTagWidth(t);
      if (i > 0) totalWidth += gapX; // add gap between items
    });
    if (totalWidth <= containerWidth) return; // all fit, no +N

    // We will need a +N pill; create a temporary one to measure width
    const tempMore = createMorePill(99); // worst-case width to reserve
    tempMore.style.visibility = 'hidden';
    container.appendChild(tempMore);
    const moreWidth = measureTagWidth(tempMore);

    // Determine how many tags can fit when reserving space for +N
    let used = 0;
    let fitCount = 0;
    for (let i = 0; i < tags.length; i++) {
      const w = measureTagWidth(tags[i]);
      const addGap = i > 0 ? gapX : 0;
      const nextUsed = used + addGap + w;
      // also consider gap before the +N (if we add it after this tag)
      const withMore = nextUsed + gapX + moreWidth;
      if (withMore <= containerWidth) {
        used = nextUsed;
        fitCount++;
      } else {
        break;
      }
    }

    // If nothing fits, show only +N and hide all tags
    const hiddenCount = tags.length - fitCount;
    if (fitCount === 0) {
      tags.forEach((t) => (t.style.display = 'none'));
      tempMore.style.visibility = '';
      tempMore.textContent = `+${hiddenCount}`;
      return;
    }

    // Show the first fitCount, hide the rest
    tags.forEach((t, i) => {
      t.style.display = i < fitCount ? '' : 'none';
    });

    // Reveal the +N pill with the real hidden count
    tempMore.style.visibility = '';
    tempMore.textContent = `+${hiddenCount}`;
  }

  function layoutAll() {
    const containers = document.querySelectorAll('.tag-row');
    containers.forEach(layoutContainer);
  }

  function rafLayoutAll() {
    // Run layout on next frame to ensure DOM is painted
    requestAnimationFrame(() => layoutAll());
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      rafLayoutAll();
      // Run again after fonts/images load for more accurate sizing
      setTimeout(rafLayoutAll, 50);
      window.addEventListener('resize', rafLayoutAll);
    });
  } else {
    rafLayoutAll();
    setTimeout(rafLayoutAll, 50);
    window.addEventListener('resize', rafLayoutAll);
  }
})();