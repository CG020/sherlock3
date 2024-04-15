

# ORDER OF PARSING:

## FOR THE FILE:

split on headers [[text]]
BUT only split if the line STARTS WITH the double brackets.
This is because certain metadata contains double brackets,
but they aren't headers


## FOR THE PAGE:

### Redirect Page:
We can just identify these by the existence of #REDIRECT in the text.
Not sure what to do with them for now.

example:
    [[Capitalist]]
    #REDIRECT Capitalism

### Unclear Page:
We can just identify these by the existence of "Title(s) may refer to:" in the text.
Not sure what to do with them for now. I'm thinking we just ignore since it does not
have any actual information.

example:
    [[Cell]]
    Cell(s) may refer to:
    ==Science and technology==
    ===Technology===
    ==Entertainment and media==
    ===Fiction===
    ===Music===
    ==Society and religion==
    ==See also==

### Normal Page:
We can just identify these by NOT being the other two ("else: normal page").

1. Title: [[in brackets]]

2. Categories (if it exists): format: "CATEGORIES: item1, item2, item3"

3. Summary -- un-denoted text that exists between CATEGORIES (if exists) and the first header

4. Headers: surrounded by double equals "==Text=="
    **NOTE THAT HEADERS AND THEIR SUBHEADERS GO 6 EQUALS DEEP SO DO NOT JUST SPLIT BY "=="
        **What I mean by that is a header "==" could have a subheader "===" that
            has a subheader "====" that has a subheader "=====" that has a subheader "======"
        That being said, we should still parse on "==" first,
            just make sure it's not followed by another equals before splitting

    ==See also==
    Only contains [tpl] [/tpl] blocks but is blank 99% of the time

    ==Images==
    Format: "Image:filename|alt_text"

    ==References==, ==Bibliography==, ==Distinctions==, ==Further reading==, ==Notes==
    All have the same format.
    Each line starts with | (0+ # of spaces before and after)
        and each source is delimited by }}.
        Note that }} doesn't always have a line break before.
        example:
        | title = text
        | first = firstname | last = lastname
        }}
        | title = text2

    ==External links==
    Seems inconsistent in the format.
    Sometimes it's text,
    Sometimes it's only subheaders (no content),
    Sometimes it follows the same format as References etc.
    I vote we just ignore it.

    ==Sources==
    Also seems inconsistent in the format.
    Sometimes it's text,
    Sometimes it follows the same format as References etc.

5.  The text within non-standard headers
    Seems to be primarily plain text with some tags interspersed.
    
    <ref>text</ref>
    Marks source references using some MLA APA type format
    IF THEY EXIST IN THE SAME LINE. I highly doubt we want to keep them,
    so we should parse them out early. A lot of them are empty anyway.
    If they're not in the same line, I have no idea. See[ref] below.
    
    [tpl]text[/tpl]
    Marks metadata blocks. Fields in these blocks
    are separated by "|". Not sure if we want to parse them
    or just ignore, but they should be removed from the text and maybe
    stored somewhere else.
    
    <blockquote>text</blockquote>
    Seems to just have text in them.
    I think we can just remove the tag and use the contents.
    
    [ref] [/ref]
    I can't for the life of me figure these out.
    SOMETIMES there's a closing tag but sometimes there isn't.
    It also seems like they're sometimes interchangeable with <ref> </ref>???? idk
    
    I'm hoping that if we just continue parsing for now we can figure it out later.

    **Some of my working theories:**
    IF there is both a [ref] and [/ref] tag, it seems the [tpl] [/tpl] tags within
    them can be treated as normal, then the rest of the tag can be treated like References etc.
    
    It seems like sometimes if [ref] doesn't have a closing [/ref],
    the rest of the line is a reference. But this is tricky because
    oftentimes the closing [/ref] tag isn't on the same line.
    
    It also seems like [/ref] exists on its own at the END of a mid-text reference.
    Example:
    The identity is seen as native to the land:
    | last = Salazar
    | first = Ruben
    | title = Who is a Chicano? And what is it the Chicanos want?
    | publisher = ''Los Angeles Times''
    | date = 1970-02-06
    }}[/ref]
    
    OR It exists at the beginning of a line and the REST of the line is a reference.
    
    Note that sometimes <ref> has more text in it. Ex: <ref name=gaz07>
