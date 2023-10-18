# Quirks & Irregularities
Syntax choices that make the grammar hard to parse and represent as AST in a
way that is easy to maintain:

- `d2:relationshipCount` is the only function expecting a quoted `UID`
- a `programRuleStringVariableName` is any string and only identifiable having
  a special meaning by its position as argument to certain functions
- `PS_EVENTDATE:` is a tag for a `UID` for a dataResolver value but does not use the
  `#{...}` wrapper and can therefore easily be confused for a named function
- functions accepting dataResolver item values do not accept all dataResolver item value types
  that can occur on top level.
- the `de:*`-functions contain `:` which is hard to distinguish from a tag
- `orgUnit.*`-functions contain `.` which is hard to distinguish from a method