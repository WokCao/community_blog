(function () {
  function getLetterIndex(ch) {
    if (!ch) return -1;
    const c = ch.toUpperCase().toUpperCase();
    const code = c.charCodeAt(0);
    if (code >= 65 && code <= 90) return code - 65; // A-Z
    return -1;
  }

  function colorForIndex(i) {
    if (i < 0) return { bg: '#E5E7EB', text: '#374151', border: '#E5E7EB' }; // gray fallback
    const hue = Math.round((i / 26) * 360);
    const bg = `hsl(${hue} 90% 90%)`;
    const text = `hsl(${hue} 40% 32%)`;
    const border = `hsl(${hue} 60% 80%)`;
    return { bg, text, border };
  }

  function applyTagColors(root = document) {
    const tags = root.querySelectorAll('.tag-pill');
    tags.forEach((el) => {
      const label = (el.textContent || '').trim();
      const idx = getLetterIndex(label.charAt(0));
      const colors = colorForIndex(idx);
      el.style.backgroundColor = colors.bg;
      el.style.color = colors.text;
      el.style.borderColor = colors.border;
      el.style.borderWidth = '1px';
      el.style.borderStyle = 'solid';
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => applyTagColors());
  } else {
    applyTagColors();
  }
})();
