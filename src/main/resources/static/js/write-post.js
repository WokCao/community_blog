const thumbnailBasePath = '/img/';
const thumbnailData = [
    ['thumbnail_1.jpg', 'Clean, modern style'],
    ['thumbnail_2.jpg', 'Organic, hand-drawn style'],
    ['thumbnail_3.jpg', 'Vibrant, colorful abstract style'],
    ['thumbnail_4.jpg', 'Minimalist, subtle textures style'],
    ['thumbnail_5.jpg', 'Digital, tech-focused style'],
    ['thumbnail_6.jpg', 'Artistic, painted style'],
    ['thumbnail_7.jpg', 'Vintage, retro aesthetic'],
    ['thumbnail_8.jpg', 'Playful, cartoon-like style'],
    ['thumbnail_9.jpg', 'Elegant, sophisticated style'],
    ['thumbnail_10.jpg', 'Cozy, community-focused style'],
]

// Thumbnail options
const thumbnailWrapper = document.getElementById('thumbnailUrl');
const preview = document.getElementById('thumbnailPreview');
thumbnailWrapper.innerHTML = '';

// Add options dynamically
thumbnailData.forEach(([filename, description]) => {
    const option = document.createElement('option');
    option.value = filename;
    option.textContent = description;
    thumbnailWrapper.appendChild(option);
    console.log(thumbnailWrapper)
});

// Function to update preview image
function updatePreview() {
    const selectedFile = thumbnailWrapper.value;
    preview.src = thumbnailBasePath + selectedFile;
    preview.alt = selectedFile;
}

// Show preview when selection changes
thumbnailWrapper.addEventListener('change', updatePreview);
thumbnailWrapper.value = 'thumbnail_1.jpg';
updatePreview();

// Rich Text Editor JavaScript
const editor = document.getElementById('editor');
const contentHidden = document.getElementById('contentHidden');

// Toolbar buttons
const boldBtn = document.getElementById('boldBtn');
const italicBtn = document.getElementById('italicBtn');
const underlineBtn = document.getElementById('underlineBtn');
const fontFamily = document.getElementById('fontFamily');
const fontSize = document.getElementById('fontSize');
const heading = document.getElementById('heading');
const textColor = document.getElementById('textColor');
const backgroundColor = document.getElementById('backgroundColor');

// Alignment buttons
const alignLeftBtn = document.getElementById('alignLeftBtn');
const alignCenterBtn = document.getElementById('alignCenterBtn');
const alignRightBtn = document.getElementById('alignRightBtn');

// List buttons
const bulletListBtn = document.getElementById('bulletListBtn');
const numberListBtn = document.getElementById('numberListBtn');

// Media buttons
const imageBtn = document.getElementById('imageBtn');
const linkBtn = document.getElementById('linkBtn');
const imageInput = document.getElementById('imageInput');

// Tags functionality
const tagInput = document.getElementById('tagInput');
const tagsContainer = document.getElementById('tagsContainer');
const tagsHidden = document.getElementById('topicsHidden');
let tags = [];

// Modern formatting helpers (no deprecated execCommand)
function getSelectionRange() {
    const sel = window.getSelection();
    let range = (sel && sel.rangeCount > 0) ? sel.getRangeAt(0) : null;

    // If there's no selection or it's outside the editor, create a caret
    // inside the editor (place it at the end) so toolbar actions still work.
    if (!range || !editor.contains(range.startContainer)) {
        // Ensure the editor has at least one child to place the caret
        if (editor.childNodes.length === 0) {
            const p = document.createElement('div');
            p.appendChild(document.createElement('br'));
            editor.appendChild(p);
        }
        editor.focus();
        const newRange = document.createRange();
        newRange.selectNodeContents(editor);
        newRange.collapse(false); // place at end
        if (sel) {
            sel.removeAllRanges();
            sel.addRange(newRange);
        }
        range = newRange;
    }
    return range;
}

function surroundSelection(tagName, attrs = {}) {
    const range = getSelectionRange();
    if (!range) return;
    const el = document.createElement(tagName);
    Object.entries(attrs).forEach(([k, v]) => {
        if (k === 'style' && typeof v === 'object') {
            Object.assign(el.style, v);
        } else {
            el.setAttribute(k, v);
        }
    });
    if (range.collapsed) {
        el.appendChild(document.createTextNode('\u200B'));
        range.insertNode(el);
    } else {
        try {
            range.surroundContents(el);
        } catch (e) {
            // Fallback: wrap extracted contents
            const frag = range.extractContents();
            el.appendChild(frag);
            range.insertNode(el);
        }
    }
    // Move caret to end of inserted element
    const sel = window.getSelection();
    sel.removeAllRanges();
    const newRange = document.createRange();
    newRange.selectNodeContents(el);
    newRange.collapse(false);
    sel.addRange(newRange);
    editor.focus();
    updateContent();
}

function applyInlineStyle(styleName, value) {
    surroundSelection('span', {style: {[styleName]: value}});
}

function unwrapElement(el) {
    if (!el || !el.parentNode) return;
    const parent = el.parentNode;
    while (el.firstChild) parent.insertBefore(el.firstChild, el);
    parent.removeChild(el);
}

function toggleInlineTag(tagName) {
    const range = getSelectionRange();
    if (!range) return;
    const el = getSelectionElement();
    const existing = el ? getNearestAncestor(el, [tagName]) : null;
    if (existing) {
        unwrapElement(existing);
        editor.focus();
        updateContent();
        updateToolbarState();
        return;
    }
    // Apply if not present
    surroundSelection(tagName);
    updateToolbarState();
}

function getBlockAncestor(node) {
    let n = node;
    while (n && n !== editor) {
        if (n.nodeType === 1) {
            const display = window.getComputedStyle(n).display;
            if (display === 'block' || display === 'list-item' || /^(P|DIV|H1|H2|H3|H4|H5|H6|LI)$/i.test(n.tagName)) {
                return n;
            }
        }
        n = n.parentNode;
    }
    return editor;
}

function setBlockTag(tagName) {
    const range = getSelectionRange();
    if (!range) return;
    const block = getBlockAncestor(range.startContainer);
    if (block === editor) {
        const el = document.createElement(tagName || 'div');
        const frag = range.extractContents();
        el.appendChild(frag);
        range.insertNode(el);
    } else {
        const replacement = document.createElement(tagName || 'div');
        // move children
        while (block.firstChild) replacement.appendChild(block.firstChild);
        block.replaceWith(replacement);
    }
    editor.focus();
    updateContent();
}

function setAlignment(alignment) {
    const range = getSelectionRange();
    if (!range) return;
    const block = getBlockAncestor(range.startContainer);
    block.style.textAlign = alignment;
    editor.focus();
    updateContent();
}

function toggleList(type) {
    const range = getSelectionRange();
    if (!range) return;
    const list = document.createElement(type === 'ol' ? 'ol' : 'ul');
    const li = document.createElement('li');
    // Extract selection content into a single list item (basic behavior)
    const frag = range.extractContents();
    if (!frag.childNodes.length) {
        li.appendChild(document.createTextNode('\u200B'));
    } else {
        li.appendChild(frag);
    }
    list.appendChild(li);
    range.insertNode(list);
    editor.focus();
    updateContent();
}

function insertLink(url) {
    const range = getSelectionRange();
    if (!range) return;
    const a = document.createElement('a');
    a.href = url;
    a.target = '_blank';
    if (range.collapsed) {
        a.textContent = url;
        range.insertNode(a);
    } else {
        const frag = range.extractContents();
        a.appendChild(frag);
        range.insertNode(a);
    }
    editor.focus();
    updateContent();
}

function insertNodeAtSelection(node) {
    const range = getSelectionRange();
    if (!range) return;
    range.insertNode(node);
    editor.focus();
    updateContent();
}

// Toolbar state utilities
function getSelectionElement() {
    const range = getSelectionRange();
    if (!range) return null;
    const node = range.startContainer;
    return node.nodeType === 1 ? node : node.parentElement;
}

function hasAncestorTag(el, tagNames) {
    let n = el;
    const set = new Set(tagNames.map(t => t.toUpperCase()));
    while (n && n !== editor && n.nodeType === 1) {
        if (set.has(n.tagName)) return true;
        n = n.parentElement;
    }
    return false;
}

function getNearestAncestor(el, tagNames) {
    let n = el;
    const set = new Set(tagNames.map(t => t.toUpperCase()));
    while (n && n !== editor && n.nodeType === 1) {
        if (set.has(n.tagName)) return n;
        n = n.parentElement;
    }
    return null;
}

function toggleBtnState(btn, active) {
    if (!btn) return;
    btn.classList.toggle('active', !!active);
    btn.setAttribute('aria-pressed', active ? 'true' : 'false');
}

function normalizeFontFamily(ff) {
    if (!ff) return '';
    return ff.split(',')[0].replace(/['"]/g, '').trim();
}

function updateToolbarState() {
    const el = getSelectionElement();
    if (!el) return;

    const comp = window.getComputedStyle(el);
    const fw = comp.fontWeight;
    const isBold = hasAncestorTag(el, ['strong', 'b']) || parseInt(fw, 10) >= 600;
    const isItalic = hasAncestorTag(el, ['em', 'i']) || comp.fontStyle === 'italic';
    const td = comp.textDecorationLine || comp.textDecoration || '';
    const isUnderline = hasAncestorTag(el, ['u']) || td.includes('underline');

    toggleBtnState(boldBtn, isBold);
    toggleBtnState(italicBtn, isItalic);
    toggleBtnState(underlineBtn, isUnderline);

    // Lists
    const inUL = !!getNearestAncestor(el, ['ul']);
    const inOL = !!getNearestAncestor(el, ['ol']);
    toggleBtnState(bulletListBtn, inUL);
    toggleBtnState(numberListBtn, inOL);

    // Alignment and heading
    const block = getBlockAncestor(el);
    const blockComp = window.getComputedStyle(block);
    const ta = (block.style && block.style.textAlign) || blockComp.textAlign || 'left';
    toggleBtnState(alignLeftBtn, ta === 'left' || ta === 'start');
    toggleBtnState(alignCenterBtn, ta === 'center');
    toggleBtnState(alignRightBtn, ta === 'right' || ta === 'end');

    // Heading select reflecting current block tag
    const tag = block.tagName ? block.tagName.toUpperCase() : '';
    if (['H1', 'H2', 'H3', 'H4', 'H5', 'H6'].includes(tag)) {
        heading.value = tag.toLowerCase();
    } else {
        heading.value = '';
    }

    // Font family/size reflect computed style if matches options
    const fam = normalizeFontFamily(comp.fontFamily);
    for (const opt of fontFamily.options) {
        if (normalizeFontFamily(opt.value).toLowerCase() === fam.toLowerCase()) {
            fontFamily.value = opt.value;
            break;
        }
    }
    const size = comp.fontSize;
    for (const opt of fontSize.options) {
        if (opt.value === size) {
            fontSize.value = size;
            break;
        }
    }
}

document.addEventListener('selectionchange', () => {
    const sel = window.getSelection();
    const anchor = sel && sel.anchorNode;
    if (anchor && editor.contains(anchor)) {
        updateToolbarState();
    }
});

// Event listeners for formatting
document.getElementById('boldBtn').addEventListener('click', () => {
    toggleInlineTag('strong');
});
document.getElementById('italicBtn').addEventListener('click', () => {
    toggleInlineTag('em');
});
document.getElementById('underlineBtn').addEventListener('click', () => {
    toggleInlineTag('u');
});

fontFamily.addEventListener('change', (e) => {
    applyInlineStyle('fontFamily', e.target.value);
    updateToolbarState();
});
fontSize.addEventListener('change', (e) => {
    applyInlineStyle('fontSize', e.target.value);
    updateToolbarState();
});

heading.addEventListener('change', (e) => {
    setBlockTag(e.target.value || 'div');
    updateToolbarState();
});

textColor.addEventListener('change', (e) => {
    applyInlineStyle('color', e.target.value);
    updateToolbarState();
});

backgroundColor.addEventListener('change', (e) => {
    applyInlineStyle('backgroundColor', e.target.value);
    updateToolbarState();
});


// Alignment
alignLeftBtn.addEventListener('click', () => {
    setAlignment('left');
    updateToolbarState();
});
alignCenterBtn.addEventListener('click', () => {
    setAlignment('center');
    updateToolbarState();
});
alignRightBtn.addEventListener('click', () => {
    setAlignment('right');
    updateToolbarState();
});

// Lists
bulletListBtn.addEventListener('click', () => {
    toggleList('ul');
    updateToolbarState();
});
numberListBtn.addEventListener('click', () => {
    toggleList('ol');
    updateToolbarState();
});

// Media insertion
imageBtn.addEventListener('click', () => imageInput.click());

imageInput.addEventListener('change', async (e) => {
    const files = e.target.files;
    for (let file of files) {
        const reader = new FileReader();
        reader.onload = (ev) => {
            const imgEl = document.createElement('img');
            imgEl.src = ev.target.result;
            imgEl.alt = 'Uploaded image';
            imgEl.style.maxWidth = '30%';
            imgEl.style.height = 'auto';
            imgEl.style.margin = '10px 0';
            imgEl.style.borderRadius = '4px';
            insertNodeAtSelection(imgEl);
        };
        reader.readAsDataURL(file);
    }
});

linkBtn.addEventListener('click', () => {
    const url = prompt('Enter URL:');
    if (url) {
        insertLink(url);
        updateToolbarState();
    }
});

// Update the hidden content field
function updateContent() {
    contentHidden.value = editor.innerHTML;
}

editor.addEventListener('input', () => {
    updateContent();
    updateToolbarState();
});

editor.addEventListener('paste', (e) => {
    e.preventDefault();

    // Get plain text from the clipboard
    const text = e.clipboardData.getData('text/plain');

    // Insert the text at the current cursor position
    const selection = window.getSelection();
    if (!selection.rangeCount) return;

    selection.deleteFromDocument(); // remove any selected text
    selection.getRangeAt(0).insertNode(document.createTextNode(text));

    // Move the cursor to the end of inserted text
    selection.collapseToEnd();
})

// Tags functionality
tagInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        const tag = e.target.value.trim();
        if (tag && !tags.includes(tag)) {
            tags.push(tag);
            updateTagsDisplay();
            e.target.value = '';
        }
    }
});

function updateTagsDisplay() {
    tagsContainer.innerHTML = '';
    tags.forEach((tag, index) => {
        const tagElement = document.createElement('span');
        tagElement.className = 'inline-flex items-center px-3 py-1 rounded-full text-sm bg-indigo-100 text-indigo-800';
        tagElement.innerHTML = `
            ${tag}
            <button type="button" class="ml-2 text-indigo-600 hover:text-indigo-800" onclick="removeTag(${index})">
                <i class="fas fa-times text-xs"></i>
            </button>
        `;
        tagsContainer.appendChild(tagElement);
    });
    tagsHidden.value = tags.join(',');
}

// Global function to remove tags
window.removeTag = function (index) {
    tags.splice(index, 1);
    updateTagsDisplay();
};

// Save and publish functionality
document.getElementById('saveBtn').addEventListener('click', () => {
    updateContent();

    const form = document.getElementById('postForm');
    const visibilitySelect = document.getElementById('visibility');
    const postTitle = document.getElementById('postTitle');

    visibilitySelect.value = 'PRIVATE';
    if (postTitle.value.trim() === '') {
        postTitle.value = 'Untitled Post';
    }

    if (Number(tags.length) === 0) {
        tagInput.focus();
        return;
    }

    if (contentHidden.value.trim() === '') {
        editor.focus();
        return;
    }

    form.submit();
});

document.getElementById('publishBtn').addEventListener('click', () => {
    updateContent();

    const form = document.getElementById('postForm');
    const visibilitySelect = document.getElementById('visibility');
    const postTitle = document.getElementById('postTitle');

    visibilitySelect.value = 'PUBLIC';
    if (postTitle.value.trim() === '') {
        postTitle.value = 'Untitled Post';
    }

    if (Number(tags.length) === 0) {
        tagInput.focus();
        return;
    }

    if (contentHidden.value.trim() === '') {
        editor.focus();
        return;
    }

    form.submit();
})

// Placeholder functionality
editor.addEventListener('focus', function () {
    if (this.innerHTML.trim() === '') {
        this.innerHTML = '';
    }
});

editor.addEventListener('blur', function () {
    if (this.innerHTML.trim() === '') {
        this.setAttribute('data-placeholder', 'Start writing your amazing post...');
    }
});

// Initial content update
updateContent();
updateToolbarState();

// Detect edit mode
const isEditMode = window.location.pathname.includes("/edit");
if (isEditMode) {
    let existingTags = tagsHidden.value;
    existingTags = existingTags.replace(/^\[|]$/g, '').trim();

    // Load existing tags
    if (existingTags) {
        tags = existingTags.split(',').map(t => t.trim());
        updateTagsDisplay();
    }

    // Set a thumbnail preview if it exists
    const selected = thumbnailWrapper.getAttribute('value') || thumbnailWrapper.value;
    const pathArray = selected.split(thumbnailBasePath);
    if (pathArray.length > 1) {
        const thumbnail = pathArray[1];
        preview.src = thumbnailBasePath + thumbnail;
        preview.alt = thumbnail;
        thumbnailWrapper.value = thumbnail;
    }

    // Update the hidden content field
    contentHidden.value = editor.innerHTML;
}