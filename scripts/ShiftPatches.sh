#!/bin/sh

if [ $# -ne 1 ]; then
  echo "Usage: $0 <shift>"
  exit 1
fi

shift_by="$1"

for f in *.patch; do
  [ -e "$f" ] || continue

  num=$(printf '%s\n' "$f" | sed -n 's/^\([0-9]\+\).*/\1/p')
  [ -n "$num" ] || continue

  rest=${f#$num}

  new_num=$((10#$num + shift_by))

  if [ "$new_num" -lt 0 ]; then
    echo "Skipping $f (resulting number < 0)"
    continue
  fi

  width=${#num}
  new_num_fmt=$(printf "%0*d" "$width" "$new_num")

  mv -- "$f" "$new_num_fmt$rest"
done
